
class MessageController < ApplicationController

  def task
    redirect_to root_path
  end
  
  def queue
    msg = params[:text] || "nothing"
    TorqueBox::Messaging::Queue.new('/queues/test').publish(msg)
    redirect_to root_path
  end

end
