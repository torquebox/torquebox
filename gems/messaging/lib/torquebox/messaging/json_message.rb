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
  module Messaging
    class JSONMessage < Message
      ENCODING = :json
      JMS_TYPE = :text

      def require_json
        # We can't ship our own json, as it may collide with the gem
        # requirement for the app.
        if !defined?( JSON )
          begin
            require 'json'
          rescue LoadError => ex
            raise RuntimeError.new( "Unable to load the json gem. Verify that is installed and in your Gemfile (if using Bundler)" )
          end
        end
      end
      
      def encode(message)
        require_json 
        @jms_message.text = JSON.fast_generate( message ) unless message.nil?
      end

      def decode
        require_json 
        JSON.parse( @jms_message.text, :symbolize_names => true ) unless @jms_message.text.nil?
      end

    end

    Message.register_encoding( JSONMessage )
  end
end
