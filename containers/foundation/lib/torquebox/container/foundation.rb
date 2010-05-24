
module TorqueBox
  module Container
    class Foundation

      MC_MAIN_DEPLOYER_NAME = "MainDeployer"

      attr_accessor :fundamental_deployment_paths
      attr_accessor :fundamental_deployments

      def initialize()
        @server = Java::org.jboss.bootstrap.api.mc.server::MCServerFactory.createServer();
  
        descriptors = @server.configuration.bootstrap_descriptors
        descriptors << Java::org.jboss.reloaded.api::ReloadedDescriptors.class_loading_descriptor
        descriptors << Java::org.jboss.reloaded.api::ReloadedDescriptors.vdf_descriptor
        @fundamental_deployment_paths = []
        @fundamental_deployments= []
      end
  
      def start
        puts "STARTING Foundation"
        @server.start
        fundamental_deployment_paths.each do |path|
          fundamental_deployments << deploy( path )
        end
        process_deployments( true )
        puts "STARTED Foundation"
      end
  
      def stop
        fundamental_deployments.reverse.each do |deployment|
          undeploy( deployment.name )
        end
        process_deployments( true )
        @server.stop
      end
  
      def deploy(path)
        virtual_file = Java::org.jboss.vfs::VFS.getChild( path )
        puts "DEPLOY VFS: #{virtual_file}"
        deployment_factory = Java::org.jboss.deployers.vfs.spi.client::VFSDeploymentFactory.instance
        deployment = deployment_factory.createVFSDeployment(virtual_file)
        main_deployer.addDeployment(deployment)
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
          main_deployer.checkComplete
        end
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
