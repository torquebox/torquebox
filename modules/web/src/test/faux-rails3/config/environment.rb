module Rails

  module VERSION
    MAJOR = 3
  end
end

=begin
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
=end

FAUX_RAILS3_ENVIRONMENT_LOADED = true

