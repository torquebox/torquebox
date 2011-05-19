require 'torquebox'

class Something
  include TorqueBox::Messaging::Backgroundable
  include TorqueBox::Injectors
  
  always_background :foo

  def initialize
    @foreground = inject("/queues/foreground")
    @background = inject("/queues/background")
  end

  def self.define_foo
    class_eval do
      define_method :foo do
        if "release" == @background.receive(:timeout => 25000)
          @foreground.publish "success"
        end
      end
    end
  end

  define_foo
end
