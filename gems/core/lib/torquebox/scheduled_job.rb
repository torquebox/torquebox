# Copyright 2008-2013 Red Hat, Inc, and individual contributors.
# 
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
# 
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public
# License along with this software; if not, write to the Free
# Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA, or see the FSF site: http://www.fsf.org.

module TorqueBox
  # This class is a Ruby API to manipulating TorqueBox scheduled jobs.
  class ScheduledJob
    class << self

      # Creates a new scheduled job.
      #
      # @param class_name The scheduled job implementation
      #                   class name
      # @param cron The cron expression defining when the job
      #             should run
      # @param options Optional parameters (a Hash), including:
      # @option options [String] :name The job name unique across the application, if not provided set to the class name
      # @option options [String] :description Job description
      # @option options [String] :timeout The time after the job execution should be interrupted. By default it'll never interrupt the job execution. Example: '2s', '1m'
      # @option options [Hash] :config Data that should be injected to the job constructor
      # @option options [boolean] :stopped If the job should be stopped after installation (default: +false+)
      # @option options [boolean] :singleton Flag to determine if the job should be executed on every node (set to `true`, default) in the cluster or only on one node (set to `false`).
      # @return [Boolean] +true+ if the job was successfully created, +false+ otherwise
      #
      # @example A simple job
      #   TorqueBox::ScheduledJob.schedule('SimpleJob', "*/10 * * * * ?")
      #
      # @example A simple job with custom name
      #   TorqueBox::ScheduledJob.schedule('SimpleJob', "*/10 * * * * ?", :name => "simple.job")
      #
      # @example Schedule a job with data to be injected to the job constructor
      #   ScheduledJob.schedule('SimpleJob', "*/10 * * * * ?", :name => "simple.config.job", :config => {:text => "text", :hash => {:a => 2}})
      #
      # @example Schedule a job stopped after creation
      #   TorqueBox::ScheduledJob.schedule('SimpleJob', "*/10 * * * * ?", :stopped => true)
      def schedule(class_name, cron, options = {})
        raise "No job class name provided" if class_name.nil?
        raise "No cron expression provided" if cron.nil?

        options = {
            :name => class_name.to_s,
            :singleton => true,
            :stopped => false,
            :timeout => "0s"
        }.merge(options)

        TorqueBox::ServiceRegistry.lookup(schedulizer_service_name).create_job(class_name.to_s, cron, options[:timeout], options[:name], options[:description], options[:config], options[:singleton], options[:stopped])
      end

      # Creates new 'at' job.
      #
      # @param class_name [String] The class name of the scheduled job to be executed
      # @param options [Hash] A hash containing the at job options:
      # @option options [Time] :at [Time] The start time of the job
      # @option options [Fixnum] :in Specifies when the job execution should start, in ms
      # @option options [Fixnum] :repeat Specifies the number of times to execute the job
      # @option options [Fixnum] :every Specifies the delay (in ms) between job executions
      # @option options [Time] :until The stop time of job execution
      # @option options [String] :name The job name unique across the application, by default set to the job class name
      # @option options [String] :description Job description
      # @option options [String] :timeout The time after the job execution should be interrupted. By default it'll never interrupt the job execution. Example: '2s', '1m'
      # @option options [Hash] :config Data that should be injected to the job constructor
      # @option options [boolean] :singleton Flag to determine if the job should be executed on every node (set to +true+) in the cluster or only on one node (set to +false+, default).
      # @return [Boolean] +true+ if the job was successfully created, +false+ otherwise
      #
      # @example Run a job every 200 ms for over 5 seconds, from now
      #   TorqueBox::AtJob.at('SimpleJob', :every => 200, :until => Time.now + 5)
      #
      # @example Start in 1 second, then every 200 ms for over 4 seconds (5 seconds from now, but start is delayed):
      #   TorqueBox::AtJob.at('SimpleJob', :at => Time.now + 1, :every => 200, :until => Time.now + 5)
      #
      # @example Start in 1 second, then every 200 ms for over 4 seconds (5 seconds from now, but start is delayed):
      #   TorqueBox::AtJob.at('SimpleJob', :in => 1_000, :every => 200, :until => Time.now + 5)
      #
      # @example Start in 1 second, then repeat te job 10 times, every 200 ms
      #   TorqueBox::AtJob.at('SimpleJob', :in => 1_000, :repeat => 10, :every => 200)
      def at(class_name, options = {})
        raise "No job class name provided" if class_name.nil?
        raise "Invalid options for scheduling the job" if options.nil? or !options.is_a?(Hash)
        raise "Invalid type for :in, should be a Fixnum" if !options[:in].nil? and !options[:in].is_a?(Fixnum)
        raise "You can't specify both :at and :in" if options.has_key?(:at) and options.has_key?(:in)
        raise "You can't specify :repeat without :every" if options.has_key?(:repeat) and !options.has_key?(:every)
        raise "You can't specify :until without :every" if options.has_key?(:until) and !options.has_key?(:every)

        options = {
            :singleton => false,
            :name => class_name,
            :timeout => "0s",
            :repeat => 0,
            :every => 0,
            :at => Time.now
        }.merge(options)

        if options.has_key?(:in)
          start = Time.now + options[:in] / 1000.0
        else
          start = options[:at]
        end

        TorqueBox::ServiceRegistry.lookup(schedulizer_service_name).create_at_job(class_name.to_s, start, options[:until], options[:every], options[:repeat], options[:timeout], options[:name], options[:description], options[:config], options[:singleton])
      end

      # Removes a scheduled job.
      #
      # This method removes the job asynchronously.
      #
      # @param name [String] The job name.
      # @return [Boolean] +true+ if the job was successfully removed, +false+ otherwise
      def remove(name)
        raise "No job name provided" if name.nil?
        raise "Couldn't find a job with name '#{name}''" if TorqueBox::ScheduledJob.lookup(name).nil?

        TorqueBox::ServiceRegistry.lookup(schedulizer_service_name).remove_job(name)
      end

      # List all scheduled jobs of this application.
      #
      # @return [Array<org.torquebox.jobs.ScheduledJob>] the list of
      #   ScheduledJob instances - see
      #   {TorqueBox::ScheduledJob.lookup} for more details on these
      #   instances
      def list
        prefix = job_service_name.canonical_name
        service_names = TorqueBox::MSC.service_names.select do |service_name|
          name = service_name.canonical_name
          name.start_with?(prefix) && !name.end_with?('mbean')
        end
        service_names.map do |service_name|
          service = TorqueBox::MSC.get_service(service_name)
          service.nil? ? nil : service.value
        end.select { |v| !v.nil? }
      end

      # Lookup a scheduled job of this application by name.
      #
      # @param [String] name the scheduled job's name (as given in
      #   torquebox.rb or torquebox.yml)
      #
      # @return [org.torquebox.jobs.ScheduledJob] The ScheduledJob instance.
      #
      # @note The ScheduledJob instances returned by this and the
      #   {TorqueBox::ScheduledJob.list} methods are not instances of
      #   this class but are instead Java objects of type
      #   org.torquebox.jobs.ScheduledJob. There are more methods
      #   available on these instances than what's shown in the
      #   example here, but only the methods shown are part of our
      #   documented API.
      #
      # @example Stop a scheduled job
      #   job = TorqueBox::ScheduledJob.lookup('my_job')
      #   job.name => 'my_job'
      #   job.started? => true
      #   job.status => 'STARTED'
      #   job.stop
      #   job.status => 'STOPPED'
      def lookup(name)
        service_name = job_service_name.append(name)
        service = TorqueBox::MSC.get_service(service_name)
        service.nil? ? nil : service.value
      end

      private

      # @api private
      def job_service_name
        TorqueBox::MSC.deployment_unit.service_name.append('scheduled_job')
      end

      # @api private
      def schedulizer_service_name
        TorqueBox::MSC.deployment_unit.service_name.append('job_schedulizer')
      end

    end
  end
end
