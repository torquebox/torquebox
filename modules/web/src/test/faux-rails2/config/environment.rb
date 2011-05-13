module Rails
  module VERSION
    MAJOR = 2
  end
end

module ActionController
  class Base
    def self.session_store 
      nil
    end
  end
  module Session
    class TorqueBoxStore
    end
  end
end

FAUX_RAILS2_ENVIRONMENT_LOADED = true

