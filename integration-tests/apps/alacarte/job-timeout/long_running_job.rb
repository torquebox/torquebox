class LongRunningJob

  def initialize(opts)
    @queue_name = opts['queue']
    @response_queue = TorqueBox::Messaging::Queue.new( @queue_name )
    @latch = java.util.concurrent.CountDownLatch.new(1)
  end

  def run()
    $stderr.puts "Job executing! " + @queue_name
    @response_queue.publish( 'started' )
    interrupted = @latch.await(30, java.util.concurrent.TimeUnit::SECONDS)
    @response_queue.publish( interrupted ? 'interrupted' : 'done', :properties => { 'completion' => 'true' } )
  end

  def on_timeout()
    $stderr.puts "Job was interrupted " + @queue_name
    @latch.count_down
  end

end
