
require 'jboss/endpoints/security_metadata'

import org.jboss.ruby.enterprise.endpoints.BaseEndpointRb

module TorqueBox
  module Endpoints
    module Base

      def self.included(target)
        puts "included into #{target}"
      end
      
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

      def log=(logger)
        @logger = logger
      end
      
      def log
        @logger
      end

      def request=(request)
        @request = request
      end
      
      def request
        @request
      end

      def principal=(principal)
        @principal = principal        
      end
      
      def principal
        @principal
      end
      
      def response_creator=(response_creator)
        @response_creator = response_creator 
      end
      
      def response_creator
        @response_creator
      end

      def create_response
        rc = response_creator
        unless ( rc.nil? )
          return eval( rc )
        end
        nil
      end
      
    end 
    
  end
end
  