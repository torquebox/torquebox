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

module ActionController
  module Session
    class TorqueBoxStore 
      
      RAILS_SESSION_KEY = '__current_rails_session'
      
      def initialize(app, options={})
        @app = app
      end 
      
      def call(env)
        load_session(env)
        status, headers, body = @app.call(env)
        commit_session(env, status, headers, body)
        return [ status, headers, body ]
      end
      
      def load_session(env)
        env['rack.session'] = load_session_data( env['java.servlet_request'].getSession(true) )
        env['rack.session.options' ] = {}
      end
      
      def commit_session(env, status, headers, body) 
        session_data = env['rack.session' ]        
        store_session_data( env['java.servlet_request'].getSession(true), session_data )
      end
      
      def load_session_data(session)
        session_data = SessionData.new
        session_data.java_session = session
        session.getAttributeNames.each do |key|
          if ( key == RAILS_SESSION_KEY )
            marshalled_bytes = session.getAttribute(RAILS_SESSION_KEY)
            if ( marshalled_bytes )
              data = Marshal.load( String.from_java_bytes( marshalled_bytes ) )
              session_data.update( data ) if Hash === data
            end
          else
            session_data[key.to_sym] = session.getAttribute(key)
          end
        end
        initial_keys = session_data.keys
        session_data[:session_id] = session.getId()
        session_data[:TORQUEBOX_INITIAL_KEYS] = initial_keys
        session_data
      end
      
      def store_session_data(session, session_data)
        hash = session_data.dup
        # java session shouldn't be marshalled
        hash.java_session = nil if hash.respond_to?(:java_session=)
        initial_keys = hash[:TORQUEBOX_INITIAL_KEYS] || []
        removed_keys = initial_keys - hash.keys 
        hash.delete(:TORQUEBOX_INITIAL_KEYS)
        hash.delete_if do |key,value|
          if ( String === key || Symbol === key )
            case value
              when String, Numeric, true, false, nil
                session.setAttribute( key.to_s, value )
                true
            else
              if value.respond_to?(:java_object)
                session.setAttribute( key.to_s, value )
                true
              else
                false
              end
            end
          end
        end
        unless hash.empty?
          marshalled_string = Marshal.dump(hash)
          marshalled_bytes = marshalled_string.to_java_bytes
          session.setAttribute(RAILS_SESSION_KEY, marshalled_bytes)
        end
        removed_keys.each do |k|
          session.removeAttribute( k.to_s )
        end
      end
    end

    class SessionData < Hash
      attr_accessor :java_session

      def url_suffix
        ";jsessionid=#{self[:session_id]}"
      end

      def destroy
        @java_session.invalidate if @java_session
      end
    end

  end
end

# This assignment allows :torquebox_store to work as a symbol
ActionController::Session::TorqueboxStore = ActionController::Session::TorqueBoxStore 

