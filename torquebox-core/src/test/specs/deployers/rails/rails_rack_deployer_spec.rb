
require 'deployers/shared_spec'

import org.torquebox.rails.web.deployers.RailsRackDeployer
import org.torquebox.rails.core.metadata.RailsApplicationMetaData
import org.torquebox.ruby.enterprise.web.rack.metadata.RackWebApplicationMetaData
import org.torquebox.ruby.enterprise.web.rack.metadata.RubyRackApplicationMetaData
import org.torquebox.ruby.enterprise.web.rack.deployers.RubyRackApplicationFactoryDeployer


describe RailsRackDeployer do
  
  it_should_behave_like "all deployers"
  
  def create_deployers
    [ 
      RailsRackDeployer.new 
    ]
  end
  
  it "should config a RackWebApplicationMetaData" do
    deployment = deploy {
      root {
      }
      attachments {
        attach( RailsApplicationMetaData ) do |md, root|
          md.setRailsRoot( root )
        end 
      }
    }
    
    unit = deployment_unit_for( deployment )
    web_metadata = unit.getAttachment( RackWebApplicationMetaData.java_class )
    
    web_metadata.should_not be_nil
    web_metadata.getHost().should be_nil
    web_metadata.getContext().should eql( '/' )
    web_metadata.getStaticPathPrefix().should eql( '/public' )
    web_metadata.getRackApplicationFactoryName().should_not be_nil
    web_metadata.getRackApplicationFactoryName().should eql( RubyRackApplicationFactoryDeployer.getBeanName( unit ) )
    web_metadata.getRackApplicationFactoryName().should match /test-deployment/
    
  end
  
end