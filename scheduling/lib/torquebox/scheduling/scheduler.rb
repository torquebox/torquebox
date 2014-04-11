module TorqueBox
  module Scheduling
    class Scheduler
      include TorqueBox::OptionUtils

      attr_accessor :scheduling_component

      WB = org.projectodd.wunderboss.WunderBoss
      WBScheduling = org.projectodd.wunderboss.scheduling.Scheduling

      def schedule(id, spec, &block)
        validate_options(spec, enum_to_set(WBScheduling::ScheduleOption))
        scheduling_component.schedule(id.to_s, block,
                                      extract_options(spec, WBScheduling::ScheduleOption))
      end

      def unschedule(id)
        scheduling_component.unschedule(id.to_s)
      end

      def start
        @scheduling_component.start
      end

      def stop
        @scheduling_component.stop
      end

      protected

      def initialize(name, options={})
        @logger = WB.logger('TorqueBox::Scheduling::Scheduler')
        validate_options(options, enum_to_set(WBScheduling::CreateOption))
        create_options = extract_options(options, WBScheduling::CreateOption)
        comp = WB.find_or_create_component(WBScheduling.java_class, name,
                                           create_options)
        @logger.debugf("TorqueBox::Scheduling::Scheduler '%s' has component %s",
                       name, comp)
        @scheduling_component = comp
        at_exit { stop }
      end
    end
  end
end
