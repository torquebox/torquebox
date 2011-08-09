require 'torquebox-messaging'

class Something
  include TorqueBox::Messaging::Backgroundable
  include TorqueBox::Injectors
  
  always_background :foo

  def initialize
    @backchannel = inject("/queues/backchannel")
  end

  def foo
    puts "FOO in Something #{Thread.current.object_id}"
    @backchannel.publish Thread.current.object_id
    nil
  end
end
