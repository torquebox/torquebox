module WunderBoss
  module TorqueBox
    class ResponseHandler
      def self.handle(rack_response, rack_responder)
        status  = rack_response[0]
        headers = rack_response[1]
        body    = rack_response[2]

        begin
          rack_responder.response_code = status.to_i

          transfer_encoding_value = nil
          headers.each { |key,value|
            rack_responder.add_header(key, value);
            transfer_encoding_value = value if key == 'Transfer-Encoding'
          }

          chunked = 'chunked' == transfer_encoding_value
          # body must respond to each and yield only String values
          # TODO: check body.to_path as a more efficient way to serve files
          body.each { |chunk|
            output = chunked ? strip_term_markers(chunk) : chunk
            unless output.nil?
              rack_responder.write(output)
              rack_responder.flush if chunked
            end
          }
        rescue NativeException => e
          # Don't needlessly raise errors because of client abort exceptions
          raise unless e.cause.toString =~ /(clientabortexception|broken pipe)/i
        ensure
          body.close if body && body.respond_to?(:close)
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
