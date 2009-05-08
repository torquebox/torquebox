
import org.jboss.ruby.enterprise.endpoints.metadata.SecurityMetaData
import org.jboss.ruby.enterprise.endpoints.metadata.InboundSecurityMetaData
import org.jboss.ruby.enterprise.endpoints.metadata.OutboundSecurityMetaData

class SecurityMetaData

  class << self
    alias :java_new :new

    def new(&block)
      object = java_new
      object.configure( &block ) if block
      object
    end
  end

  def configure(&block)
    instance_eval &block if block 
  end

  def inbound(&block)
    inbound = InboundSecurityMetaData.new(&block)
    setInboundSecurityMetaData( inbound )
  end

  def outbound(&block)
    outbound = InboundSecurityMetaData.new(&block)
    setOutboundSecurityMetaData( outbound )
  end

end

class InboundSecurityMetaData

  class << self
    alias :java_new :new

    def new(&block)
      object = java_new
      object.configure( &block ) if block
      object
    end
  end

  def configure(&block)
    instance_eval &block if block
  end

  def verify_timestamp
    self.setVerifyTimestamp( true )
  end

  def verify_signature
    self.setVerifySignature( true )
  end

  def trust_store(store_name)
    self.setTrustStore( store_name.to_s )
  end

end

class OutboundSecurityMetaData

  def initialize(&block)
    super()
    instance_eval &block if block
  end

end

