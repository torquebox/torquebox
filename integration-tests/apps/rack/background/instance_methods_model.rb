require 'torquebox-messaging'

class InstanceMethodsModel
  include TorqueBox::Messaging::Backgroundable

  always_background :foo, :future_ttl => 100_000

  def initialize
    @foreground = TorqueBox.fetch("queue/foreground")
    @background = TorqueBox.fetch("queue/background")
  end

  def foo
  end

  def bar
    if "release" == @background.receive(:timeout => 25000)
      @foreground.publish "success"
    end
    nil
  end
  
  def self.define_foo
    class_eval do
      define_method :foo do
        if "release" == @background.receive(:timeout => 25000)
          @foreground.publish "success"
        end
        'foo'
      end
    end
  end

  define_foo
end
