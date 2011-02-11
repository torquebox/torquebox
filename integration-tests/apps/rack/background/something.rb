require 'org.torquebox.torquebox-messaging-client'

class Something

  include TorqueBox::Messaging::Backgroundable
  always_background :foo

  def initialize
    @foreground = TorqueBox::Messaging::Queue.new("/queues/foreground")
    @background = TorqueBox::Messaging::Queue.new("/queues/background")
  end

  def foo
    if "release" == @background.receive(:timeout => 25000)
      @foreground.publish "success"
    end
  end
end
