
require 'deployers/shared_spec'

import org.torquebox.ruby.enterprise.endpoints.cxf.deployers.CXFBusDeployer
import org.torquebox.ruby.enterprise.endpoints.cxf.RubyCXFBus
import org.torquebox.ruby.enterprise.endpoints.metadata.RubyEndpointsMetaData

describe CXFBusDeployer do
  
  it_should_behave_like "all deployers"
  
  def create_deployers
    [ 
      CXFBusDeployer.new
    ]
  end
  
  it "should create a CXF Bus BMD if a RubyEndpointsMetaData is attached" do
    deployment = deploy {
      root {
      }
      attachments {
        attach( RubyEndpointsMetaData ) 
      }
    }
    
    unit = deployment_unit_for( deployment )
    bmd = bmd_for( unit, RubyCXFBus )
    bmd.should_not be_nil
  end
  
  
  it "should not create a CXF Bus BMD if no RubyEndpointsMetaData is attached" do
    deployment = deploy {
      root {
      }
    }
    
    unit = deployment_unit_for( deployment )
    bmd = bmd_for( unit, RubyCXFBus )
    bmd.should be_nil
  end
  
end