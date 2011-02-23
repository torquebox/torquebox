module TorqueBox
  module Authenticator
    def self.[](name)
      ::TorqueBox::Kernel.lookup(TORQUEBOX_APP_NAME + "-" + authentication + "-" + name)
    end
  end
end
