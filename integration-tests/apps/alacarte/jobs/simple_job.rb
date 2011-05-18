class SimpleJob

  include TorqueBox::Injectors
  
  def run() 
    queue = inject( 'queue/response' )
    $stderr.puts "Job executing! queue is #{queue}"
    queue.publish( 'done' )
  end

end
