module TorqueBox
  module Messaging
    # @api private
    module Helpers
      WB = org.projectodd.wunderboss.WunderBoss
      WBDestination = org.projectodd.wunderboss.messaging.Destination
      WBMessaging = org.projectodd.wunderboss.messaging.Messaging
      WBQueue = org.projectodd.wunderboss.messaging.Queue
      WBTopic = org.projectodd.wunderboss.messaging.Topic
      WBConnection = org.projectodd.wunderboss.messaging.Connection
      WBSession = org.projectodd.wunderboss.messaging.Session

      protected

      def default_broker
        WB.find_or_create_component(WBMessaging.java_class, 'default', nil)
      end
    end
  end
end
