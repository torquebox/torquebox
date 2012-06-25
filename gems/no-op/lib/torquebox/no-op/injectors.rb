module TorqueBox
  module Injectors
    def inject(*args)
      fetch(*args)
    end

    def fetch(*args)
      DummyResource.new
    end

    class DummyResource
      def publish(*args)
        # no-op
      end
      def receive(*args)
        # no-op
      end
    end
  end
end