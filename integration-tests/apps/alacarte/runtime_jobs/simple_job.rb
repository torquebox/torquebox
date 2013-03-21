class SimpleJob

  def initialize(opts = {})
    @options = opts
    @response_queue = TorqueBox.fetch(@options["queue"])
  end

  def run
    @response_queue.publish(:state => :running, :options => @options)
  end
end
