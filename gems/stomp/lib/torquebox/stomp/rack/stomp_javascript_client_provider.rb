
module TorqueBox
  module Stomp

    class StompJavascriptClientProvider
   
      def initialize(app)
        @app = app
      end

      def call(env)
        path_info = env['PATH_INFO']
        if ( path_info == '/stilts-stomp.js' )
          return javascript_client_response
        else
          return @app.call( env )
        end
      end

      def javascript_client_response
        js = File.read( File.join( File.dirname(__FILE__), 'stilts-stomp-client-js.js' ) )
        [ 200,
          { 'Content-Length' => "#{js.size}",
            'Content-Type'   => 'text/plain' },
          js ]
      end

    end 

  end
end
