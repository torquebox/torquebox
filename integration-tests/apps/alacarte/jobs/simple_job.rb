require 'torquebox-messaging'

class SimpleJob
  
  def run() 
    $stderr.puts "Job executing!"
    TorqueBox::Messaging::Queue.new( '/queue/response' ).publish( 'done' )
  end

end
