require 'jruby/core_ext'

module Spec
  module Example
    module ExampleGroupMethods

      def deploy path, options = {}
        @run_mode = options.fetch(:run_mode, :client)
        @deployment = path
      end
      attr_reader :deployment
      
      def run_mode
        case @run_mode
        when :client
          org.jboss.arquillian.api.RunModeType::AS_CLIENT
        when :container
          org.jboss.arquillian.api.RunModeType::IN_CONTAINER
        else
          raise "Unknown :run_mode -- acceptable values are :client or :container"
        end 
      end

      def create_deployment 
        tail = @deployment.split('/')[-1]
        base = /(.*)\./.match(tail)[1]
        archive = org.jboss.shrinkwrap.api.ShrinkWrap.create( org.jboss.shrinkwrap.api.spec.JavaArchive.java_class, "#{base}.jar" )
        deploymentDescriptorUrl = JRuby.runtime.jruby_class_loader.getResource( name )
        archive.addResource( deploymentDescriptorUrl, "/META-INF/#{tail}" )
        archive
      end

    end
  end
end

Spec::Runner.configure do |config|

  config.before(:all) do
    self.class.add_class_annotation( org.jboss.arquillian.api.Run => { "value" => self.class.run_mode } )
    # TODO: As of JRuby 1.5.3, class methods are ignored by become_java! so this won't work:
    self.class.add_method_annotation( "create_deployment", org.jboss.arquillian.api.Deployment => {} )
    @real_java_class = self.class.become_java!

    configuration = org.jboss.arquillian.impl.XmlConfigurationBuilder.new.build()
    @adaptor = org.jboss.arquillian.impl.DeployableTestBuilder.build(configuration)
    @adaptor.beforeSuite
    @adaptor.beforeClass(@real_java_class)
  end

  config.after(:all) do
    @adaptor.afterClass(@real_java_class)
    @adaptor.afterSuite
  end

end

