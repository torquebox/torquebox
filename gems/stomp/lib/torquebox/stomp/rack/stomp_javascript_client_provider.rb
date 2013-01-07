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
