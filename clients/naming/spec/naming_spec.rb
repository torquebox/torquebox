
require 'torquebox/naming'

describe TorqueBox::Naming do

  describe "configuration" do 

    before(:each) do
      java.lang::System.clearProperty( 'java.naming.provider.url' )
      java.lang::System.clearProperty( 'java.naming.factory.initial' )
      java.lang::System.clearProperty( 'java.naming.factory.url.pkgs' )
    end

    it "should set appropriate localhost-based defaults during configuration" do
      TorqueBox::Naming.configure
      java.lang::System.getProperty( 'java.naming.provider.url' ).should eql( 'jnp://localhost:1099/' )
      java.lang::System.getProperty( 'java.naming.factory.initial' ).should eql( 'org.jnp.interfaces.NamingContextFactory' )
      java.lang::System.getProperty( 'java.naming.factory.url.pkgs' ).should eql( 'org.jboss.naming:org.jnp.interfaces' )
    end

    it "should allow custom settings for default configuration" do
      TorqueBox::Naming.configure do |config|
        config.host = 'otherhost.com'
        config.port = '11099'
      end
      java.lang::System.getProperty( 'java.naming.provider.url' ).should eql( 'jnp://otherhost.com:11099/' )
      java.lang::System.getProperty( 'java.naming.factory.initial' ).should eql( 'org.jnp.interfaces.NamingContextFactory' )
      java.lang::System.getProperty( 'java.naming.factory.url.pkgs' ).should eql( 'org.jboss.naming:org.jnp.interfaces' )
    end

  end

  describe "operations" do
    before(:each) do
      TorqueBox::Naming.configure do |config|
        config.host = '10.42.42.11'
      end
    end

    it "should allow binding and retrieving of named objects" do
      obj = "Howdy"
      TorqueBox::Naming['my_object'] = obj
      fetched_obj = TorqueBox::Naming['my_object']
      fetched_obj.should eql( obj )
    end
  end


end
