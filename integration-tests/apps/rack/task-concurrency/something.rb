require 'torquebox-messaging'

class Something

  include TorqueBox::Messaging::Backgroundable
  always_background :foo

  def initialize
    @backchannel = TorqueBox::Messaging::Queue.new("/queues/backchannel")
  end

  def foo
    @backchannel.publish Thread.current.object_id
  end
end
