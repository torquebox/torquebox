
require 'jboss/endpoints/security_metadata'

import org.jboss.ruby.enterprise.endpoints.BaseEndpointRb

module JBoss
module Endpoints

class BaseEndpoint < BaseEndpointRb

  def self.target_namespace(ns=nil)
    ( @target_namspace = ns ) if ( ns != nil )
    @target_namspace ||= nil
  end

  def self.port_name(pn=nil)
    ( @port_name = pn ) if ( pn != nil )
    @port_name ||= nil
  end

  def self.security(&block) 
    unless block.nil?
      @security = SecurityMetaData.new( &block )
    end
    @security
  end

  def log
    self.getLogger()
  end

  def request
    self.getRequest()
  end

  def principal
    self.getPrincipal()
  end

  def create_response
    rc = self.getResponseCreator()
    unless ( rc.nil? )
      return eval rc
    end
    nil
  end

end

end # Endpoints
end # JBoss
