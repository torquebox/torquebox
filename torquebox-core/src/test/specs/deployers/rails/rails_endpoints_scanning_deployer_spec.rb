
require 'deployers/shared_spec'

import org.torquebox.rails.endpoints.deployers.RailsEndpointsScanningDeployer
import org.torquebox.rails.core.metadata.RailsApplicationMetaData
import org.torquebox.ruby.enterprise.endpoints.metadata.RubyEndpointsMetaData


describe RailsEndpointsScanningDeployer do
  
  it_should_behave_like "all deployers"
  
  def create_deployers
    [ 
      RailsEndpointsScanningDeployer.new 
    ]
  end
  
  it "should only scan app/endpoints/** if RailsAppMetaData exists" do
    deployment = deploy {
       root {
         dir( "app/endpoints" ) { 
           file( 'foo.wsdl' )
           file( 'foo_endpoint.rb' )
         }
       }
     }
     unit       = deployment_unit_for( deployment )
     meta_data  = unit.getAttachment( RubyEndpointsMetaData.java_class )
     
     meta_data.should be_nil
  end
  
  it "should accept all .wsdl with a matching _endpoint.rb" do
    deployment = deploy {
      root {
        dir( "app/endpoints" ) { 
          file( 'foo.wsdl' )
          file( 'foo_endpoint.rb' )
          file( 'bar.wsdl' )
          file( 'bar_endpoint.rb' )
        }
      }
      attachments {
        attach( RailsApplicationMetaData ) do |md, root|
          md.setRailsRoot( root )
        end 
      }
    }
    unit       = deployment_unit_for( deployment )
    meta_data  = unit.getAttachment( RubyEndpointsMetaData.java_class )
    
    meta_data.should_not be_nil
    meta_data.getEndpoints().size().should eql( 2 )
    
    endpoint = meta_data.getEndpointByName( "foo" )
    endpoint.should_not be_nil
    endpoint.getEndpointClassName().should eql( "FooEndpoint" )
    endpoint.getClassLocation().should eql( "foo_endpoint" )
    
    endpoint = meta_data.getEndpointByName( "bar" )
    endpoint.should_not be_nil
    endpoint.getEndpointClassName().should eql( "BarEndpoint" )
    endpoint.getClassLocation().should eql( "bar_endpoint" )
  end
  
  it "should ignore any .wsdl without a matching _endpoint.rb" do
    deployment = deploy {
       root {
         dir( "app/endpoints" ) { 
           file( 'foo.wsdl' )
           file( 'bar.wsdl' )
           file( 'bar_endpoint.rb' )
         }
       }
       attachments {
         attach( RailsApplicationMetaData ) do |md, root|
           md.setRailsRoot( root )
         end 
       }
    }
    unit       = deployment_unit_for( deployment )
    meta_data  = unit.getAttachment( RubyEndpointsMetaData.java_class )
     
    meta_data.should_not be_nil
    meta_data.getEndpoints().size().should eql( 1 )
     
    endpoint = meta_data.getEndpointByName( "bar" )
    endpoint.should_not be_nil
    endpoint.getEndpointClassName().should eql( "BarEndpoint" )
    endpoint.getClassLocation().should eql( "bar_endpoint" )
  end
  
  it "should accept any *_endpoint.rb even without a matching .wsdl" do
    deployment = deploy {
       root {
         dir( "app/endpoints" ) { 
           file( 'foo_endpoint.rb' )
           file( 'bar.wsdl' )
           file( 'bar_endpoint.rb' )
         }
       }
       attachments {
         attach( RailsApplicationMetaData ) do |md, root|
           md.setRailsRoot( root )
         end 
       }
    }
    unit       = deployment_unit_for( deployment )
    meta_data  = unit.getAttachment( RubyEndpointsMetaData.java_class )
     
    meta_data.should_not be_nil
    meta_data.getEndpoints().size().should eql( 2 )
     
    endpoint = meta_data.getEndpointByName( "foo" )
    endpoint.should_not be_nil
    endpoint.getEndpointClassName().should eql( "FooEndpoint" )
    endpoint.getClassLocation().should eql( "foo_endpoint" )
    
    endpoint = meta_data.getEndpointByName( "bar" )
    endpoint.should_not be_nil
    endpoint.getEndpointClassName().should eql( "BarEndpoint" )
    endpoint.getClassLocation().should eql( "bar_endpoint" )
  end
  
  it "should recursively search for endpoints" do
    deployment = deploy {
      root {
        dir( "app/endpoints" ) { 
          dir( "package_one" ) {
            file( 'foo.wsdl' )
            file( 'foo_endpoint.rb' )
          }
          dir( "package_two" ) {
            file( 'bar.wsdl' )
            file( 'bar_endpoint.rb' )
          }
        }
      }
      attachments {
        attach( RailsApplicationMetaData ) do |md, root|
          md.setRailsRoot( root )
        end 
      }
    }
    unit       = deployment_unit_for( deployment )
    meta_data  = unit.getAttachment( RubyEndpointsMetaData.java_class )
    
    meta_data.should_not be_nil
    meta_data.getEndpoints().size().should eql( 2 )
    
    endpoint = meta_data.getEndpointByName( "package_one/foo" )
    endpoint.should_not be_nil
    endpoint.getEndpointClassName().should eql( "PackageOne::FooEndpoint" )
    endpoint.getClassLocation().should eql( "package_one/foo_endpoint" )
    
    endpoint = meta_data.getEndpointByName( "package_two/bar" )
    endpoint.should_not be_nil
    endpoint.getEndpointClassName().should eql( "PackageTwo::BarEndpoint" )
    endpoint.getClassLocation().should eql( "package_two/bar_endpoint" )
  end
  
  
end