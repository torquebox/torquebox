require 'container'
require 'open-uri'

describe "basic rack test" do

  class << self 
    def create_deployment()
      deployment = "rack/1.1.0/basic-rack.yml"
      puts "BOB: DEPLOYING deployment = #{deployment}"
      tail = deployment.split('/')[-1]
      puts "BOB: DEPLOYING tail = #{tail}"
      base = /(.*)\./.match(tail)[1]
      puts "BOB: DEPLOYING base = #{base}"
      archive = org.jboss.shrinkwrap.api.ShrinkWrap.create( org.jboss.shrinkwrap.api.spec.JavaArchive.java_class, "#{base}.jar" )
      puts "BOB: DEPLOYING archive = #{archive}"
      deploymentDescriptorUrl = JRuby.runtime.jruby_class_loader.getResource( deployment )
      puts "BOB: DEPLOYING url = #{deploymentDescriptorUrl}"
      archive.addResource( deploymentDescriptorUrl, "/META-INF/#{tail}" )
      puts "Deploying #{archive}"
      archive
    end

    add_method_signature("create_deployment", [org.jboss.shrinkwrap.api.spec.JavaArchive.to_java])
    add_method_annotation( "create_deployment", org.jboss.arquillian.api.Deployment => {} )
  end

  it "should not get a 500 server error" do
    #result = open("http://localhost:8080/basic-rack").read
  end

end
