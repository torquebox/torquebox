require '/Users/jim/local/torquebox/jruby/lib/ruby/gems/1.8/gems/org.torquebox.torquebox-container-foundation-1.0.0.RC1-java/lib/jboss-reloaded-vdf-bootstrap-minimal-0.1.2.jar'

class MessageController < ApplicationController

  def task
    redirect_to root_path
  end
  
  def queue
    msg = params[:text] || "nothing"
    TorqueBox::Messaging::Queue.new('/queues/test').publish(msg)
    redirect_to root_path
  end


  # Test programmitic creation/destruction of queues

  def start
    @@queue = TorqueBox::Messaging::Queue.new "/queues/#{params[:name]}"
    @@queue.start
    puts "JC: about to create dispatcher"
    @@dispatcher = TorqueBox::Messaging::Dispatcher.new do
      map TestConsumer, @@queue
      puts "JC: consumers mapped"
    end
    @@dispatcher.start
    puts "JC: dispatcher started"
  rescue Exception 
    puts "JC: wtf? #{$!} #{$@}"
  end

  def stop
    @@dispatcher.stop
    @@queue.destroy
  end

end
