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

module TorqueBox
  module Rack
    class EnvironmentBuilder
      def self.build(servlet_context, servlet_request, input, errors)
        ($servlet_context=servlet_context) unless defined?( $servlet_context ) 
        context_path = servlet_request.context_path
        servlet_path = servlet_request.servlet_path
        path_info    = servlet_request.path_info
        env = {}
        env['REQUEST_METHOD']    = servlet_request.getMethod()
        env['SCRIPT_NAME']       = "#{context_path}#{servlet_path}"
        env['PATH_INFO']         = path_info
        env['QUERY_STRING']      = servlet_request.getQueryString() || ''
        env['SERVER_NAME']       = servlet_request.getServerName()
        env['SERVER_PORT']       = servlet_request.getServerPort()
        env['CONTENT_TYPE']      = servlet_request.getContentType()
        env['CONTENT_LENGTH']    = servlet_request.getContentLength()
        env['REQUEST_URI']       = "#{context_path}#{servlet_path}#{path_info}"
        env['REMOTE_ADDR']       = servlet_request.getRemoteAddr()
        env['rack.version']      = [ 0, 1 ]
        env['rack.multithread']  = true
        env['rack.multiprocess'] = true
        env['rack.run_once']     = false
        env['rack.input']        = input
        env['rack.errors']       = errors
        servlet_request.getHeaderNames().each do |name|
          env_name = name.upcase.gsub( /-/, '_' )
          env["HTTP_#{env_name}"] = servlet_request.getHeader( name )
        end
        env['servlet_request']      = servlet_request
        # and again, for jruby-rack compatibility
        env['java.servlet_request'] = servlet_request
        env
      end
    end
  end
end
