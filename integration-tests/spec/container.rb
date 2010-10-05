
module TorqueBox
  module TestContainer

    def start name
      configuration = org.jboss.arquillian.impl.XmlConfigurationBuilder.new.build()
      serviceLoader = org.jboss.arquillian.impl.DynamicServiceLoader.new
      @container = serviceLoader.onlyOne( org.jboss.arquillian.spi.DeployableContainer.java_class )
      @container.setup( nil, configuration )
      @container.start( nil )
      @deployment = create_deployment( name )
      @container.deploy( nil, @deployment )
    end

    def stop
      @container.undeploy( nil, @deployment )
      @container.stop( nil )
    end

    def create_deployment name
      tail = name.split('/')[-1]
      base = /(.*)\./.match(tail)[1]
      archive = org.jboss.shrinkwrap.api.ShrinkWrap.create( org.jboss.shrinkwrap.api.spec.JavaArchive.java_class, "#{base}.jar" )
      deploymentDescriptorUrl = JRuby.runtime.jruby_class_loader.getResource( name )
      archive.addResource( deploymentDescriptorUrl, "/META-INF/#{tail}" )
      archive
    end

  end
end
