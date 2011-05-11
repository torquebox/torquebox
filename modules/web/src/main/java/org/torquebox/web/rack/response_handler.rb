# Copyright 2008-2011 Red Hat, Inc, and individual contributors.
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
    class ResponseHandler
      def self.handle(rack_response, servlet_response)
        status  = rack_response[0]
        headers = rack_response[1]
        body    = rack_response[2]
        
        begin
          status_code = status.to_i
          servlet_response.setStatus( status_code )
          
          headers.each{|key,value|
            if value.respond_to?( :each ) 
              value.each { |v| servlet_response.addHeader( key, v ) }
            else
              servlet_response.addHeader( key, value )
            end
          }
          out = servlet_response.getOutputStream()

          if body.respond_to?( :each )
            body.each { |chunk| out.write( chunk.to_java_bytes ) }
          else
            out.write( body.to_java_bytes )
          end
        ensure
          body.close if body && body.respond_to?( :close )
        end
      end
    end
  end
end
