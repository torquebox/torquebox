class SimpleJob
  def initialize(opts = {})
    @response_queue = TorqueBox.fetch("/queue/response")
  end

  def run
    puts "Running #{self} job..."
    @response_queue.publish("Job executed!")
  end
end