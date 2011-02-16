require 'org.torquebox.messaging-client'

class SampleTask < TorqueBox::Messaging::Task
  def foo(index)
    backchannel = TorqueBox::Messaging::Queue.new("/queues/backchannel")
    backchannel.publish "at#{index}-sleep"
    sleep 3
    backchannel.publish "at#{index}-awake"
  end
end
