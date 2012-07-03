class LongRunningJob

  def initialize(opts)
    @queue_name = opts['queue']
    @response_queue = TorqueBox::Messaging::Queue.new( @queue_name )
    @interrupted = false

  end

  def run()
    $stderr.puts "Job executing! " + @queue_name
    @response_queue.publish( 'started' )
    sleep( 3 )
    @response_queue.publish( @interrupted ? 'interrupted' : 'done', :properties => { 'completion' => 'true' } )
  end

  def on_timeout()
    $stderr.puts "Job was interrupted " + @queue_name
    @interrupted = true
  end

end
