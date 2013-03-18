class SimpleJob
  include TorqueBox::Injectors

  def initialize(opts = {})
    @options = opts

    puts "Job options: #{@options.inspect}"

    @response_queue = fetch(@options["queue"])
  end

  def run
    puts "Running #{self} job..."
    @response_queue.publish(:state => :running, :options => @options)
  end
end