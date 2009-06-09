
require 'deployers/shared_spec'

import org.torquebox.rails.runtime.deployers.RailsRubyRuntimeFactoryDescriber
import org.torquebox.rails.runtime.deployers.RailsRuntimeInitializer
import org.torquebox.rails.core.metadata.RailsApplicationMetaData
import org.torquebox.ruby.core.runtime.metadata.RubyRuntimeMetaData
import org.torquebox.ruby.core.runtime.metadata.RubyLoadPathMetaData


describe RailsRubyRuntimeFactoryDescriber do
  
  it_should_behave_like "all deployers"
  
  def create_deployers
    [ 
      RailsRubyRuntimeFactoryDescriber.new 
    ]
  end
  
  it "should add the RAILS_ROOT to the load path" do
    deployment = deploy {
      root {
      }
      attachments {
        attach( RailsApplicationMetaData ) do |md, root|
          md.setRailsRoot( root )
        end 
      }
    }
    unit             = deployment_unit_for( deployment )
    runtime_metadata = unit.getAttachment( RubyRuntimeMetaData.java_class )
    
    runtime_metadata.should_not be_nil
    load_paths = runtime_metadata.getLoadPaths()
    load_paths.size.should eql( 1 )
    
    load_paths.first.getURL().should eql( unit.getRoot().toURL() )
  end
  
  it "should add railties lib to the load path" do
    deployment = deploy {
      root {
        dir( "vendor/rails/railties/lib" ) {
          file( "railties.rb" )
        }
      }
      attachments {
        attach( RailsApplicationMetaData ) do |md, root|
          md.setRailsRoot( root )
        end 
      }
    }
    unit             = deployment_unit_for( deployment )
    runtime_metadata = unit.getAttachment( RubyRuntimeMetaData.java_class )
    
    runtime_metadata.should_not be_nil
    load_paths = runtime_metadata.getLoadPaths()
    load_paths.size.should eql( 2 )
    
    load_paths[0].getURL().should eql( unit.getRoot().toURL() )
    load_paths[1].getURL().should eql( unit.getRoot().getChild( "vendor/rails/railties/lib" ).toURL() )
  end
  
  it "should provide an initializer" do
    deployment = deploy {
      attachments {
        attach( RailsApplicationMetaData ) do |md, root|
          md.setRailsRoot( root )
        end 
      }
    }
    
    unit             = deployment_unit_for( deployment )
    runtime_metadata = unit.getAttachment( RubyRuntimeMetaData.java_class )
    
    runtime_metadata.should_not be_nil
    
    initializer = runtime_metadata.getRuntimeInitializer()
    initializer.should_not be_nil
    initializer.class.should eql( RailsRuntimeInitializer )
    initializer.getRailsRoot().should eql( unit.getRoot() )
  end
  
  it "should set the baseDir to the RAILS_ROOT" do
    deployment = deploy {
      attachments {
        attach( RailsApplicationMetaData ) do |md, root|
          md.setRailsRoot( root )
        end 
      }
    }
    
    unit             = deployment_unit_for( deployment )
    runtime_metadata = unit.getAttachment( RubyRuntimeMetaData.java_class )
    
    runtime_metadata.should_not be_nil
    runtime_metadata.getBaseDir().should_not be_nil
    runtime_metadata.getBaseDir().should eql( unit.getRoot() )
  end
  
end