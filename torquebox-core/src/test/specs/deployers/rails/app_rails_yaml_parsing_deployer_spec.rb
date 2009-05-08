
require 'deployers/shared_spec'

import org.torquebox.rails.core.deployers.AppRailsYamlParsingDeployer
import org.torquebox.rails.core.metadata.RailsApplicationMetaData

describe AppRailsYamlParsingDeployer do
  
  it_should_behave_like "all deployers"
  
  def create_deployers
    [ 
      AppRailsYamlParsingDeployer.new 
    ]
  end
  
  it "should create a sub-deployment with pre-attached RailsApplicationMetaData" do
    deployment = deploy( 'toplevel/simple-rails.yml' )
    unit = deployment_unit_for( deployment )
    
    sub_deployment = unit.getAttachment( "jboss.rails.root.deployment" )
    sub_deployment.should_not be_nil
    sub_unit =  deployment_unit_for( sub_deployment )
    
    meta_data = sub_unit.getAttachment( RailsApplicationMetaData.java_class )
    meta_data.should_not be_nil
    
    meta_data.getRailsRootPath().should eql( '/Users/bob/oddthesis/oddthesis' )
    meta_data.getRailsEnv().should eql( 'development' )
  end
  
end