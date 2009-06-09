
require 'deployers/shared_spec'

import org.torquebox.rails.core.deployers.RailsRootRecognizingDeployer
import org.torquebox.rails.core.metadata.RailsApplicationMetaData

describe RailsRootRecognizingDeployer do
  
  it_should_behave_like "all deployers"
  
  def create_deployers
    [ 
      RailsRootRecognizingDeployer.new 
    ]
  end
  
  it "should trigger by config/environment.rb presence" do
    deployment = deploy {
      root {
        dir( 'config' ) {
          file( 'environment.rb' )
        }
      }
    }
    unit       = deployment_unit_for( deployment )
    meta_data  = unit.getAttachment( RailsApplicationMetaData.java_class )
    
    meta_data.should_not be_nil
    meta_data.getRailsRoot().should eql( unit.getRoot() )
  end
  
  it "should not trigger by config/**" do
    deployment = deploy {
      root {
        dir( 'config' ) {
          file( 'README.txt' )
        }
      }
    }
    unit       = deployment_unit_for( deployment )
    meta_data  = unit.getAttachment( RailsApplicationMetaData.java_class )
    
    meta_data.should be_nil
  end
  
end