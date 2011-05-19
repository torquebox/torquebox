require 'torquebox-messaging'

class SampleTask < TorqueBox::Messaging::Task
  include TorqueBox::Injectors
  
  def foo(index)
    backchannel = inject("queue/backchannel")
    puts "FOO in SampleTask #{Thread.current.object_id}"
    backchannel.publish Thread.current.object_id
  end
end
