require 'torquebox'

class Something
  include TorqueBox::Messaging::Backgroundable

  always_background :foo

  def initialize
    @foreground = TorqueBox.fetch("/queues/foreground")
    @background = TorqueBox.fetch("/queues/background")
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
