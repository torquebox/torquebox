
module TorqueBox
  module Stomp
    class Message 

      def self.new(body, headers={})
        message = org.projectodd.stilts.stomp::StompMessages.createStompMessage()
        message.content_as_string = body

        headers.each do |k,v|
          message.headers[k.to_s] = v.to_s
        end

        message
      end

    

    end
  end
end
