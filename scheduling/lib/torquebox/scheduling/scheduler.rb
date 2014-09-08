# Copyright 2014 Red Hat, Inc, and individual contributors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


module TorqueBox
  module Scheduling
    # Provides an interface to a job scheduler.
    #
    # You can get access to the default or a custom configured
    # scheduler via {.find_or_create}, and schedule against that
    # instance, or use {.schedule} and {.unschedule} to interact with
    # the default scheduler.
    class Scheduler
      include TorqueBox::OptionUtils

      # @return The raw java scheduler object.
      attr_reader :internal_scheduler

      # Schedules a job to fire at some point(s) in the future.
      #
      # If called with the id of an already scheduled job, that job
      # will be replaced.
      #
      # Time options (`:at`, `:until`) can be specified as `Time`
      # objects or millis-since-epoch.
      #
      # Period options (`:in`, `:every`) are milliseconds, but can be
      # generated with ActiveSupport's numeric additions (if your
      # application uses ActiveSupport):
      #
      #     s.schedule(:foo, in: 5.minutes, every: 10.seconds) {
      #       puts "Called!"
      #     }
      #
      # @param id [String, Symbol] The identifier for the job.
      # @param spec [Hash] Options specifying the firing schedule for the job.
      # @option spec :in [Fixnum] a period (in millis) after which the job will fire
      # @option spec :at [Time, Fixnum] a time after which the job will fire
      # @option spec :every [Fixnum] the period (in millis) between firings
      # @option spec :until [Time, Fixnum] a specific time for the job to stop firing
      # @option spec :limit [Fixnum] limits the firings to a specific count
      # @option spec :cron [String] fires according to a
      #   {http://quartz-scheduler.org/documentation/quartz-2.2.x/tutorials/tutorial-lesson-06 Quartz-style}
      #   cron spec
      # @option spec :singleton [true, false] (true) denotes the job's behavior in a cluster
      # @param block [Proc] A zero-arity block or proc that will be
      #   called on each job execution.
      # @return [Job] An object that allows you to unschedule the job.
      def schedule(id, spec, &block)
        validate_options(spec, opts_to_set(WBScheduling::ScheduleOption))
        spec = coerce_schedule_options(spec)
        replacement = internal_scheduler.schedule(id.to_s, block,
                                                  extract_options(spec, WBScheduling::ScheduleOption))
        Job.new(self, id, replacement)
      end

      # Unschedules the job with the given id.
      #
      # @param id [String, Symbol] The id of the job to unschedule
      # @return true if a job was unscheduled, false otherwise
      def unschedule(id)
        internal_scheduler.unschedule(id.to_s)
      end

      # Starts the scheduler.
      #
      # The scheduler will automatically be started when a job is
      # scheduled, so you may never need to call this.
      def start
        @internal_scheduler.start
      end

      # Stops the scheduler after unscheduling all of its jobs.
      def stop
        @internal_scheduler.stop
      end

      # Looks up the scheduler with the given name.
      #
      # If a scheduler with that name doesn't exist, it is created
      # with the given options. The options are ignored when
      # retrieving an existing scheduler.
      #
      # @param name [String, Symbol] The name of the scheduler. The
      #   default scheduler is named 'default'.
      # @param options [Hash] Options for scheduler creation.
      # @option options num_threads [Fixnum] (5) The size of the thread
      #   pool for firing jobs
      def self.find_or_create(name, options = {})
        Scheduler.new(name, options)
      end

      # (see #schedule)
      # This method schedules via the default scheduler.
      def self.schedule(id, spec, &block)
        default_scheduler.schedule(id, spec, &block)
      end

      # (see #unschedule)
      # This method unschedules via the default scheduler.
      def self.unschedule(id)
        default_scheduler.unschedule(id)
      end

      protected

      WB = org.projectodd.wunderboss.WunderBoss
      WBScheduling = org.projectodd.wunderboss.scheduling.Scheduling

      def self.default_scheduler
        @scheduler ||= find_or_create("default")
      end

      def initialize(name, options = {})
        @logger = WB.logger('TorqueBox::Scheduling::Scheduler')
        validate_options(options, opts_to_set(WBScheduling::CreateOption))
        create_options = extract_options(options, WBScheduling::CreateOption)
        comp = WB.find_or_create_component(WBScheduling.java_class, name,
                                           create_options)
        @logger.debug("TorqueBox::Scheduling::Scheduler '{}' has component {}",
                      name, comp)
        @internal_scheduler = comp
        at_exit { stop }
      end

      def coerce_schedule_options(options)
        options.clone.merge(options) do |k, v|
          # ActiveSupport's durations use seconds as the base unit, so
          # we have to detect that and convert to ms
          v = v.in_milliseconds if defined?(ActiveSupport::Duration) && v.is_a?(ActiveSupport::Duration)

          v = as_date(v) if [:at, :until].include?(k)

          v = !!v if k == :singleton

          v.to_java
        end
      end

      def as_date(val)
        if val.is_a?(Integer)
          Time.at(val)
        else
          val
        end
      end

    end
  end
end
