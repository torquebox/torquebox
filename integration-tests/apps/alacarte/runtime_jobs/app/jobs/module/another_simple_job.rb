module SomeModule
  class AnotherSimpleJob
    include TorqueBox::Injectors

    def initialize(opts = {})
      @options = opts
      @response_queue = fetch(@options["queue"])
    end

    def run
      @response_queue.publish(:state => :running, :options => @options)
    end
  end
end