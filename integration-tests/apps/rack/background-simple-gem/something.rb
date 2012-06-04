require 'torquebox'

class Something
  include TorqueBox::Messaging::Backgroundable
  include TorqueBox::Injectors
  
  always_background :foo

  def initialize
    @foreground = fetch("/queues/foreground")
    @background = fetch("/queues/background")
  end

  def self.define_foo
    class_eval do
      define_method :foo do
        if "release" == @background.receive(:timeout => 120_000)
          @foreground.publish "success"
        end
        nil
      end
    end
  end

  define_foo
end
