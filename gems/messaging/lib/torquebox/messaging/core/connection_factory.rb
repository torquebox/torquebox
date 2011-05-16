
module TorqueBox
  module Messaging
    module Core
      class ConnectionFactory

        attr_reader :jms_connection_factory

        def initialize(jms_connection_factory)
          @jms_connection_factory = jms_connection_factory
        end

        def with_new_connection(&block)
          connection = create_connection
          connection.start
          begin
            result = block.call( connection )
          ensure
            connection.close
          end
          return result
        end

        def create_connection()
          Connection.new( @jms_connection_factory.create_connection )
        end

        def to_s
          "[ConnectionFactory: jms_connection_factory=#{jms_connection_factory}]"
        end

      end
    end
  end
end
