
require 'torquebox/endpoints/security_metadata'

module TorqueBox
  module Endpoints

    class Configuration
      def initialize(&block)
        instance_eval &block if block
      end

      def target_namespace(ns=nil)
        ( @target_namspace = ns ) if ( ns != nil )
        @target_namspace ||= nil
      end
  
      def port_name(pn=nil)
        ( @port_name = pn ) if ( pn != nil )
        @port_name ||= nil
      end

      def security(&block) 
        unless block.nil?
          @security = SecurityMetaData.new( &block )
        end
        @security
      end
    end

    module Base

      def self.included(into)
        # puts "enhancing #{into}"
        class << into
          def endpoint_configuration(&block)
            unless block.nil?
              @configuration = Configuration.new( &block ) 
            end
            @configuration
          end
        end
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
  

