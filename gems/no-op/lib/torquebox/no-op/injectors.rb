module TorqueBox
  module Injectors
    def inject(*args)
      lookup(*args)
    end

    def lookup(*args)
      DummyResource.new
    end

    class DummyResource
      def publish(*args)
        # no-op
      end
    end
  end
end