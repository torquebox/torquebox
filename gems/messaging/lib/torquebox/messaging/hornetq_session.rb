module TorqueBox
  module Messaging

    class HornetQSession < Session

      # if we direct connect to HQ, we have to provide an actual
      # HornetQDestination instead of a destination name
      def java_destination(destination)
        type = destination.is_a?( Queue ) ? 'queue' : 'topic'
        org.hornetq.jms.client.HornetQDestination.from_address( "jms.#{type}.#{destination.jms_destination}" )
      end
      
    end
    
  end
end

