class SimpleJob
  include TorqueBox::Injectors

  def initialize(opts = {})
    @options = opts
    puts "Job options: #{@options}"
  end

  def run
    puts "Running #{self} job..."
    @response_queue.publish(:state => :running, :options => @options)
  end
end