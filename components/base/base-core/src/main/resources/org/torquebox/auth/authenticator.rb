module TorqueBox
  module Authentication
    def self.[](name)
      Authenticator.new(::TorqueBox::Kernel.lookup(TORQUEBOX_APP_NAME + "-" + authentication + "-" + name))
    end

    def self.default
      self['default']
    end
  end

  class Authenticator
    def initialize(auth_bean)
      @auth_bean = auth_bean
    end

    def authenticate(user, pass, &block)
      authenticated = @auth_bean.authenticate(user, pass)
      block.call if block
      authenticated
    end
  end
end
