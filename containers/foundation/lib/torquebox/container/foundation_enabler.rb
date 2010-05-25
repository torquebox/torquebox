
module TorqueBox
  module Container
    class FoundationEnabler

      def fundamental_deployment_paths
        [ File.join( File.dirname(__FILE__), 'foundation-jboss-beans.xml' ) ]
      end
  
    end
  end
end
