# JBoss, Home of Professional Open Source
# Copyright 2009, Red Hat Middleware LLC, and individual contributors
# by the @authors tag. See the copyright.txt in the distribution for a
# full listing of individual contributors.
#
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
#
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this software; if not, write to the Free
# Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA, or see the FSF site: http://www.fsf.org.

module JBoss
  module Rack
    class SipEnvironmentBuilder
      def self.build_env_request(servlet_context, servlet_request, sip_ruby_controller_name, errors)
        ($servlet_context=servlet_context) unless defined?( $servlet_context ) 
        env = {}
        env['REQUEST_METHOD']    = servlet_request.getMethod()
        env['REQUEST_URI']       = servlet_request.getRequestURI() 
        env['rack.version']      = [ 0, 1 ]
        env['rack.multithread']  = true
        env['rack.multiprocess'] = true
        env['rack.run_once']     = false        
        env['rack.errors']       = errors
        env['sip_ruby_controller_name'] = sip_ruby_controller_name
        servlet_request.getHeaderNames().each do |name|
          env_name = name.upcase.gsub( /-/, '_' )
          env["SIP_#{env_name}"] = servlet_request.getHeader( name )
        end
        env['sip_servlet_message']      = servlet_request
        # and again, for jruby-rack compatibility
        env['java.sip_servlet_message'] = servlet_request
        env
      end
      
      def self.build_env_response(servlet_context, servlet_response, sip_ruby_controller_name, errors)
        ($servlet_context=servlet_context) unless defined?( $servlet_context ) 
        env = {}
        env['REQUEST_METHOD']    = servlet_response.getMethod()
        env['STATUS_CODE']       = servlet_response.getStatus() 
        env['REASON_PHRASE']       = servlet_response.getReasonPhrase()
        env['rack.version']      = [ 0, 1 ]
        env['rack.multithread']  = true
        env['rack.multiprocess'] = true
        env['rack.run_once']     = false        
        env['rack.errors']       = errors
        env['sip_ruby_controller_name'] = sip_ruby_controller_name
        servlet_response.getHeaderNames().each do |name|
          env_name = name.upcase.gsub( /-/, '_' )
          env["SIP_#{env_name}"] = servlet_response.getHeader( name )
        end
        env['sip_servlet_message']      = servlet_response
        # and again, for jruby-rack compatibility
        env['java.sip_servlet_message'] = servlet_response
        env
      end
    end
  end
end
