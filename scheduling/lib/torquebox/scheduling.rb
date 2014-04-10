require 'torquebox/scheduling/scheduler'

module TorqueBox
  module Scheduling


    def self.find_or_create(name, options={})
      Scheduler.new(name, options)
    end

    def self.schedule(id, spec, &block)
      scheduler.schedule(id, spec, &block)
    end

    def self.unschedule(id)
      scheduler.unschedule(id)
    end

    protected

    def self.scheduler
      @scheduler ||= find_or_create("default")
    end

  end
end
