
require 'deployers/shared_spec'

import org.torquebox.ruby.enterprise.crypto.deployers.CryptoYamlParsingDeployer
import org.torquebox.ruby.enterprise.crypto.metadata.CryptoMetaData

describe CryptoYamlParsingDeployer do
  
  it_should_behave_like "all deployers"
  
  def create_deployers
    @deployer =  CryptoYamlParsingDeployer.new()
    @deployer.setStoreBasePath( 'path/to/crypto/' )
    [ 
      @deployer
    ]
  end
  
  it "should use adjust based upon base-path" do
    deployment = deploy {
      root {
        dir( 'config', :metadata=>true ) {
          file 'crypto.yml', :read=>"crypto/multi-crypto.yml"
        }
      }
    }
    unit       = deployment_unit_for( deployment )
    meta_data  = unit.getAttachment( CryptoMetaData.java_class )
    
    meta_data.should_not be_nil
    
    [ 'keystore', 'truststore' ].each do |name|
      store = meta_data.getCryptoStore( name )
      store.should_not be_nil
      store.getStore().should eql( "path/to/crypto/auth/#{store.name}.jks" )
      store.getPassword().should eql( "foobar4#{name}" )
    end
    
    store = meta_data.getCryptoStore( "otherstore" )
    store.should_not be_nil
    store.getStore().should eql( "/fq/path/to/otherstore.jks" )
    store.getPassword().should eql( "foobar4otherstore" )
  end
  
end