require 'torquebox-messaging'

class SampleTask < TorqueBox::Messaging::Task

  def foo(index)
    backchannel = TorqueBox.fetch("/queues/backchannel")
    puts "FOO in SampleTask #{Thread.current.object_id}"
    backchannel.publish Thread.current.object_id
    nil
  end
end
