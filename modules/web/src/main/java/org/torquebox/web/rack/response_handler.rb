# Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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
            if value.respond_to?( :each_line )
              value.each_line { |v| add_header( servlet_response, key, v.chomp("\n") ) }
            elsif value.respond_to?( :each )
              value.each { |v| add_header( servlet_response, key, v.chomp("\n") ) }
            else
              add_header( servlet_response, key, value )
            end
          }
          out = servlet_response.getOutputStream()

          if body.respond_to?( :each_line ) || body.respond_to?( :each )
            chunked = headers.fetch( 'Transfer-Encoding', '' ) == 'chunked'
            body.send( body.respond_to?( :each_line ) ? :each_line : :each ) { |chunk|
              output = chunked ? strip_term_markers( chunk ) : chunk
              unless output.nil?
                out.write( output.to_java_bytes )
                out.flush if chunked
              end
            }
          else
            out.write( body.to_java_bytes )
          end
          servlet_response.flush_buffer
        rescue NativeException => e
          # Don't needlessly raise errors because of client abort exceptions
          raise unless e.cause.toString =~ /(clientabortexception|broken pipe)/i
        ensure
          body.close if body && body.respond_to?( :close )
        end
      end

      def self.add_header(servlet_response, key, value)
        # Leave out the transfer-encoding header since the container takes
        # care of chunking responses and adding that header
        unless key == "Transfer-Encoding" && value == "chunked"
          servlet_response.addHeader( key, value )
        end
      end

      def self.strip_term_markers(chunk)
        # Heavily copied from jruby-rack's rack/response.rb
        term = "\r\n"
        tail = "0#{term}#{term}".freeze
        term_regex = /^([0-9a-fA-F]+)#{Regexp.escape(term)}(.+)#{Regexp.escape(term)}/mo
        if chunk == tail
          # end of chunking, do nothing
          nil
        elsif chunk =~ term_regex
          # format is (size.to_s(16)) term (chunk) term
          # if the size doesn't match then this is some
          # output that just happened to match our regex
          if $1.to_i(16) == $2.bytesize
            $2
          else
            chunk
          end
        else
          chunk
        end
      end
    end
  end
end
