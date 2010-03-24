require 'base64'

module TorqueBox
  module Messaging
    module Dispatcher
      def self.dispatch(listener_class_name, session, message)
        listener_class = eval listener_class_name
        listener = listener_class.new
        listener.session = session if ( listener.respond_to?( "session=" ) )
        message_type = message.get_string_property( 'torqueboxMessageType')
        if ( message_type == 'task' ) 
          encoded = message.text
          serialized = Base64.decode64( encoded )
          object = Marshal.restore( serialized )
          task = message.get_string_property( 'torqueboxTask' )
          if ( ! listener.respond_to?( task.to_sym ) )
            task = 'execute'
          end
          method = listener.method( task.to_sym )
          if ( method.arity == 2 ) 
            method.call( object, message )
          else
            method.call( object )
          end
        elsif ( message_type == 'object' && listener.respond_to?( :on_object ) )
          encoded = message.text
          serialized = Base64.decode64( encoded )
          object = Marshal.restore( serialized )
          method = listener.method( :on_object )  
          if ( method.arity == 2 ) 
            method.call( object, message )
          else
            method.call( object )
          end
        elsif ( message_type == 'text' && listener.respond_to?( :on_text ) )
          object = message.text
          method = listener.method( :on_text )  
          if ( method.arity == 2 ) 
            method.call( object, message )
          else
            method.call( object )
          end
        else
          listener.on_message( message )
        end
      end 
    end
  end
end
