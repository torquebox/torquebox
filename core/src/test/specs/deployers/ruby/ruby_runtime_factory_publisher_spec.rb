
require 'deployers/shared_spec'

import org.torquebox.ruby.core.runtime.deployers.RubyRuntimeFactoryPublisher
import org.torquebox.ruby.core.runtime.metadata.RubyRuntimeMetaData
import org.torquebox.ruby.core.runtime.spi.RubyRuntimeFactory
import org.torquebox.ruby.core.runtime.DefaultRubyRuntimeFactory
import org.torquebox.ruby.core.runtime.RubyRuntimeFactoryProxy

describe RubyRuntimeFactoryPublisher do
  
  it_should_behave_like "all deployers"
  
  def create_deployers
    [
      RubyRuntimeFactoryPublisher.new()
    ]
  end
  
  it "should proxy the attached factory as a named MCBean" do
    factory = DefaultRubyRuntimeFactory.new
    deployment = deploy {
      attachments {
        attach_object( RubyRuntimeFactory, factory )
      }
    }
    unit = deployment_unit_for( deployment )
    
    bmd = bmd_for( unit, RubyRuntimeFactoryProxy )
    bmd.should_not be_nil
    bmd.getName().should eql( RubyRuntimeFactoryPublisher.getBeanName( unit ) )
  end
  
end