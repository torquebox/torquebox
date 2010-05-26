require 'org.torquebox.torquebox-container-foundation'
require 'torquebox/container/foundation'

module TorqueBox
  module Container
    class MessagingEnabler

      def initialize(&block)
        instance_eval( &block ) if block
      end

      def fundamental_deployment_paths()
        [ File.join( File.dirname(__FILE__), 'messaging-jboss-beans.xml' ) ]
      end

      def before_start(container)
        Java::java.lang::System.setProperty( 'java.naming.provider.url',
                                             'jnp://10.42.42.11:1099/' )
        Java::java.lang::System.setProperty( 'java.naming.factory.initial',
                                             'org.jnp.interfaces.NamingContextFactory' )
        Java::java.lang::System.setProperty( 'java.naming.factory.url.pkgs',
                                             'org.jboss.naming:org.jnp.interfaces' )

        Java::java.lang::System.setProperty( "torquebox.hornetq.configuration.url", 
                                             "file://" + File.join( File.dirname(__FILE__), 'hornetq-configuration.xml' ) )

      end
    
      def after_start(container)
        rmi_class_provider = container['RMIClassProvider']
        server_socket = rmi_class_provider.server_socket
        puts "ServerSocket=#{server_socket}"
        port=server_socket.local_port
        puts "PORT: #{port}"
        Java::java.lang::System.setProperty( 'java.rmi.server.codebase', "http://10.42.42.11:#{port}/" )
        class_loader = Java::java.lang::Thread.currentThread().getContextClassLoader()
        codebase_url=rmi_class_provider.addClassLoader( class_loader )
        puts "codebase_url[#{codebase_url}]"
      end

    end
  end
end
