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

      # List all scheduled jobs of this application.
      #
      # @return [Array<org.torquebox.jobs.ScheduledJob>] the list of
      #   ScheduledJob instances - see
      #   {TorqueBox::ScheduledJob.lookup} for more details on these
      #   instances
      def list
        prefix = job_prefix.canonical_name
        service_names = TorqueBox::MSC.service_names.select do |service_name|
          name = service_name.canonical_name
          name.start_with?(prefix) && !name.end_with?('mbean')
        end
        service_names.map do |service_name|
          TorqueBox::MSC.get_service(service_name).value
        end
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
        service_name = job_prefix.append(name)
        service = TorqueBox::MSC.get_service(service_name)
        service.nil? ? nil : service.value
      end

      private

      def job_prefix
        TorqueBox::MSC.deployment_unit.service_name.append('scheduled_job')
      end

    end
  end
end
