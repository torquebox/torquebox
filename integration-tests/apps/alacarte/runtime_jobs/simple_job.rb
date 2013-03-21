class SimpleJob

  def initialize(opts = {})
    @options = opts

    puts "Job options: #{@options.inspect}"

    @response_queue = TorqueBox.fetch(@options["queue"])
  end

  def run
    puts "Running #{self} job..."
    @response_queue.publish(:state => :running, :options => @options)
  end
end
