
require 'deployers/shared_spec'

import org.torquebox.rails.core.deployers.RailsVersionDeployer
import org.torquebox.rails.core.metadata.RailsVersionMetaData
import org.torquebox.rails.core.metadata.RailsApplicationMetaData

describe RailsVersionDeployer do
  
  it_should_behave_like "all deployers"
  
  def create_deployers
    [ 
      RailsVersionDeployer.new 
    ]
  end
  
  it "should parse version.rb" do
    deployment = deploy {
      root {
        dir( "vendor/rails/railties/lib/rails" ) { 
          file( 'version.rb', :read=>'rails-version.rb' ) 
        }
      }
      attachments {
        attach( RailsApplicationMetaData ) do |md, root|
          md.setRailsRoot( root )
        end 
      }
    }
    unit       = deployment_unit_for( deployment )
    meta_data  = unit.getAttachment( RailsVersionMetaData.java_class )
    
    meta_data.should_not be_nil
    
    meta_data.getMajor().should eql( 2 )
    meta_data.getMinor().should eql( 4 )
    meta_data.getTiny().should  eql( 9 )
  end
  
  it "should complain appropriately if version.rb cannot be found" do
    deployment = deploy {
      attachments {
        attach( RailsApplicationMetaData ) do |md, root|
          md.setRailsRoot( root )
        end 
      }
    }
    
    unit = deployment_unit_for( deployment )
    
    unit.should_not be_nil
    
    errored = error_contexts()
    
    errored.should_not be_empty
    
    exception = errored[ unit.getRoot().toURL().toExternalForm() ]
    
    exception.should_not be_nil
    
    exception.getMessage().should match( /.*vendorized.*/ )
    
  end
  
end