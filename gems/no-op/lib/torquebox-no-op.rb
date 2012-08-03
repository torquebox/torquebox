require "torquebox/no-op/messaging"
require "torquebox/no-op/stomp"
require "torquebox/no-op/injectors"

module TorqueBox
  def self.transaction(*args)
    yield if block_given?
  end
end
