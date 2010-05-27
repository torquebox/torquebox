
require 'ostruct'
require 'torquebox/container/foundation_enabler'

module TorqueBox
  module Container
    class Foundation

      MC_MAIN_DEPLOYER_NAME = "MainDeployer"

      def initialize()
        @logger = org.jboss.logging::Logger.getLogger( 'org.torquebox.containers.Foundation' )
        @server = Java::org.jboss.bootstrap.api.mc.server::MCServerFactory.createServer();
  
        descriptors = @server.configuration.bootstrap_descriptors
        descriptors << Java::org.jboss.reloaded.api::ReloadedDescriptors.class_loading_descriptor
        descriptors << Java::org.jboss.reloaded.api::ReloadedDescriptors.vdf_descriptor
        descriptors << org.jboss.bootstrap.api.descriptor::FileBootstrapDescriptor.new( java.io::File.new( File.join( File.dirname(__FILE__), 'foundation-bootstrap-jboss-beans.xml' ) ) )
        @enablers = []
        enable( FoundationEnabler )
      end

      def enable(enabler_or_class,&block)
        enabler = nil
        if ( enabler_or_class.is_a?( Class ) ) 
          enabler = enabler_or_class.new( &block )
        else
          enabler = enabler_or_class
        end
        
        wrapper = OpenStruct.new( :enabler=>enabler, :deployments=>[] )
        @enablers << wrapper
      end
  
      def start
        puts "STARTING Foundation"
        @server.start

        beans_xml = File.join( File.dirname(__FILE__), 'foundation-jboss-beans.xml' )

        @enablers.each do |wrapper|
          if ( wrapper.enabler.respond_to?( :before_start ) ) 
            wrapper.enabler.send( :before_start, self )
          end
          wrapper.enabler.fundamental_deployment_paths.each do |path|
            wrapper.deployments << deploy(path)
          end
        end

        process_deployments( true )

        @enablers.each do |wrapper|
          if ( wrapper.enabler.respond_to?( :after_start ) )  
            wrapper.enabler.send( :after_start, self )
          end
        end



        puts "STARTED Foundation"
      end
  
      def stop
        puts "STOPPING MCServer"
        puts "Undeploying enablers"
        @enablers.reverse.each do |wrapper|
          wrapper.deployments.each do |deployment|
            puts "undeploying #{deployment.inspect}"
            name = Java::java.lang::String.new( deployment.name )
            puts "name=#{name}"
            undeploy( name )
          end
        end
     
        process_deployments( true )
        puts "Stopping core container"
        @server.stop
        puts "core container stopped"
      end
  
      def deploy(path)
        virtual_file = Java::org.jboss.vfs::VFS.getChild( path )
        puts "DEPLOY VFS: #{virtual_file}"
        deployment_factory = Java::org.jboss.deployers.vfs.spi.client::VFSDeploymentFactory.instance
        deployment = deployment_factory.createVFSDeployment(virtual_file)
        main_deployer.addDeployment(deployment)
        #deployment_unit = deployment_unit( deployment )
        #deployment_unit.addAttachment( JRuby.runtime.java_class.to_java, JRuby.runtime )
        deployment
      end

      def undeploy(deployment_name)
        main_deployer.undeploy( deployment_name )
      end

      def process_deployments(check_complete=false)
        puts "PROCESS DEPLOYMENTS"
        main_deployer.process
        if ( check_complete )
          puts "checking completeness"
          begin
            main_deployer.checkComplete
          rescue => e
            @logger.error( e.to_s, e.cause )
            raise e
          end
        end
      end

      def deployment_unit(name) 
        main_deployer.getDeploymentUnit( name )
      end

      def kernel()
        kernel_controller.kernel
      end

      def kernel_controller() 
        @server.kernel.controller
      end

      def main_deployer()
        kernel_controller.getInstalledContext(MC_MAIN_DEPLOYER_NAME).target
      end
     
      def [](bean_name)
        entry = kernel_controller.getInstalledContext( bean_name )
        return nil if entry.nil?
        entry.target
      end
  
    end
  end
end
