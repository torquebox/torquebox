
module TorqueBox
  module Jobs
    module Base

      def log
        @logger
      end

      def log=(logger)
        @logger = logger
      end

    end
  end
end
