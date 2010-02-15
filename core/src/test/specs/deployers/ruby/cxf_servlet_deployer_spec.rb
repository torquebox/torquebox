
require 'deployers/shared_spec'

import org.torquebox.ruby.enterprise.endpoints.cxf.deployers.CXFServletDeployer
import org.torquebox.ruby.enterprise.endpoints.metadata.RubyEndpointsMetaData
import org.jboss.metadata.web.jboss.JBossWebMetaData;

describe CXFServletDeployer do
  
  it_should_behave_like "all deployers"
  
  def create_deployers
    [ 
      CXFServletDeployer.new
    ]
  end
  
  it "should create a CXF Servlet BMD if a RubyEndpointsMetaData is attached" do
    deployment = deploy {
      root {
      }
      attachments {
        attach( RubyEndpointsMetaData ) 
      }
    }
    
    unit = deployment_unit_for( deployment )
    web_metadata = unit.getAttachment( JBossWebMetaData.java_class )
    web_metadata.should_not be_nil
    
    servlet = web_metadata.getServlets().get( CXFServletDeployer::SERVLET_NAME )
    servlet.should_not be_nil
    
    mapping = web_metadata.getServletMappings().get( 0 )
    mapping.should_not be_nil
    mapping.getUrlPatterns().should_not be_empty
    mapping.getUrlPatterns().size.should eql( 1 )
    
    url_pattern = mapping.getUrlPatterns().get( 0 )
    url_pattern.should eql( "/endpoints/*" )
  end
  
  
  it "should not create a CXF Servlet BMD if no RubyEndpointsMetaData is attached" do
    deployment = deploy {
      root {
      }
    }
    
    unit = deployment_unit_for( deployment )
    web_metadata = unit.getAttachment( JBossWebMetaData.java_class )
    web_metadata.should be_nil
  end
  
end