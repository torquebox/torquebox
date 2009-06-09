
require 'deployers/shared_spec'

import org.jboss.metadata.web.jboss.JBossWebMetaData
import org.torquebox.ruby.enterprise.web.rack.deployers.RackWebApplicationDeployer
import org.torquebox.ruby.enterprise.web.rack.metadata.RackWebApplicationMetaData

describe RackWebApplicationDeployer do
  
  it_should_behave_like "all deployers"
  
  def create_deployers
    [
      RackWebApplicationDeployer.new
    ]
  end
  
  it "should configure a new JBossWebMetaData" do
    deployment = deploy {
      attachments {
        attach( RackWebApplicationMetaData ) do |md, root|
          md.setContext( "/" )
          md.setRackApplicationFactoryName( "test-factory" )
          md.setStaticPathPrefix( "/public" )
        end
      }
    }
    unit = deployment_unit_for( deployment )
    
    web_metadata = unit.getAttachment( JBossWebMetaData.java_class )
    
    web_metadata.should_not be_nil
    web_metadata.getContextRoot().should eql( "/" )
    
    filter = web_metadata.getFilters().get( RackWebApplicationDeployer::FILTER_NAME )
    filter.should_not be_nil
    
    mapping = web_metadata.getFilterMappings().get( 0 )
    mapping.should_not be_nil
    mapping.getUrlPatterns().should_not be_empty
    mapping.getUrlPatterns().size.should eql( 1 )
    
    url_pattern = mapping.getUrlPatterns().get( 0 )
    url_pattern.should eql( "/*" )
    
    servlet = web_metadata.getServlets().get( RackWebApplicationDeployer::SERVLET_NAME )
    servlet.should_not be_nil
    
    mapping = web_metadata.getServletMappings().get( 0 )
    mapping.should_not be_nil
    mapping.getUrlPatterns().should_not be_empty
    mapping.getUrlPatterns().size.should eql( 1 )
    
    url_pattern = mapping.getUrlPatterns().get( 0 )
    url_pattern.should eql( "/*" )
    
    servlet.getInitParam().size.should eql( 1 )
    static_root = servlet.getInitParam().get( 0 )
    static_root.should_not be_nil
    static_root.getParamName().should eql( "resource.root" )
    static_root.getParamValue().should eql( "/public" )
  end
  
  it "should configure a new JBossWebMetaData with a non-root context" do
    deployment = deploy {
      attachments {
        attach( RackWebApplicationMetaData ) do |md, root|
          md.setContext( "/some-context/" )
          md.setRackApplicationFactoryName( "test-factory" )
          md.setStaticPathPrefix( "/public" )
        end
      }
    }
    unit = deployment_unit_for( deployment )
    
    web_metadata = unit.getAttachment( JBossWebMetaData.java_class )
    
    web_metadata.should_not be_nil
    web_metadata.getContextRoot().should eql( "/some-context/" )
    
    filter = web_metadata.getFilters().get( RackWebApplicationDeployer::FILTER_NAME )
    filter.should_not be_nil
    
    mapping = web_metadata.getFilterMappings().get( 0 )
    mapping.should_not be_nil
    mapping.getUrlPatterns().should_not be_empty
    mapping.getUrlPatterns().size.should eql( 1 )
    
    url_pattern = mapping.getUrlPatterns().get( 0 )
    url_pattern.should eql( "/*" )
  end
  
  it "should not configure the static servlet if no static prefix is set" do
    deployment = deploy {
      attachments {
        attach( RackWebApplicationMetaData ) do |md, root|
          md.setContext( "/some-context/" )
          md.setRackApplicationFactoryName( "test-factory" )
        end
      }
    }
    unit = deployment_unit_for( deployment )
    
    web_metadata = unit.getAttachment( JBossWebMetaData.java_class )
    
    web_metadata.getServlets().should be_empty
  end
  
end