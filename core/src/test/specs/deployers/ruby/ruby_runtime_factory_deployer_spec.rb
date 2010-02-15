
require 'deployers/shared_spec'

import org.torquebox.ruby.core.runtime.deployers.RubyRuntimeFactoryDeployer
import org.torquebox.ruby.core.runtime.metadata.RubyRuntimeMetaData
import org.torquebox.ruby.core.runtime.spi.RubyRuntimeFactory

describe RubyRuntimeFactoryDeployer do
  
  it_should_behave_like "all deployers"
  
  def create_deployers
    deployer =  RubyRuntimeFactoryDeployer.new()
    deployer.setKernel( @kernel )
    [
      deployer
    ]
  end
  
  it "should create and attach a factory" do
    deployment = deploy {
      attachments {
        attach( RubyRuntimeMetaData ) do |md, root|
          md.setBaseDir( root ) 
        end
      }
    }
    unit = deployment_unit_for( deployment )
    
    factory = unit.getAttachment( RubyRuntimeFactory.java_class )
    factory.should_not be_nil
    factory.getApplicationName().should eql( "test-deployment" )
    factory.getKernel().should_not be_nil
    factory.getClassLoader().should_not be_nil
  end
  
end