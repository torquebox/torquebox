require 'torquebox-messaging'

class ClassMethodsModel
  include TorqueBox::Messaging::Backgroundable
  
  always_background :foo

  def self.init
    @foreground = TorqueBox::Messaging::Queue.new("queue/foreground")
    @background = TorqueBox::Messaging::Queue.new("queue/background")
  end

  def self.foo
  end

  def self.bar
    if "release" == @background.receive(:timeout => 25000)
      @foreground.publish "success"
    end
    nil
  end
  
  def self.define_foo
    class_eval do
      class << self
        define_method :foo do
          if "release" == @background.receive(:timeout => 25000)
            @foreground.publish "success"
          end
          nil
        end
      end
    end
  end

  init
  define_foo
end


