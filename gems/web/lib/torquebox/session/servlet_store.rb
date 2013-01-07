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
  module Session
    class ServletStore 
      
      RAILS_SESSION_KEY = '__current_rails_session'
      SYMBOL_KEYS       = '__torquebox_symbol_keys'
      
      def initialize(app, options={})
        @app = app
      end 
      
      def call(env)
        ServletStore.load_session(env)
        status, headers, body = @app.call(env)
        ServletStore.commit_session(env, status, headers, body)
        return [ status, headers, body ]
      end
      
      def self.load_session(env)
        env['rack.session'] = load_session_data( env['java.servlet_request'].getSession(true) )
        env['rack.session.options' ] = {}
      end
      
      def self.commit_session(env, status, headers, body) 
        session_data = env['rack.session' ]        
        ServletStore.store_session_data( env['java.servlet_request'].getSession(true), session_data )
      end
      
      def self.load_session_data(session)
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
            session_data[key] = session.getAttribute(key)
          end
        end
        symbolize_keys!(session_data)
        initial_keys = session_data.keys
        session_data[:session_id] = session.getId()
        session_data[:TORQUEBOX_INITIAL_KEYS] = initial_keys
        session_data
      end

      def self.symbolize_keys!(session_data)
        symbol_keys = session_data[ SYMBOL_KEYS ] || []
        keys = session_data.keys
        keys.each do |key|
          if ( symbol_keys.include?( key ) ) 
            session_data[ key.to_sym ] = session_data.delete( key ) 
          end
        end
      end
      
      def self.store_session_data(session, session_data)
        hash = session_data.dup
        # java session shouldn't be marshalled
        hash.java_session = nil if hash.respond_to?(:java_session=)
        initial_keys = hash[:TORQUEBOX_INITIAL_KEYS] || []
        removed_keys = initial_keys - hash.keys 
        symbol_keys = []
        hash.delete(:TORQUEBOX_INITIAL_KEYS)
        hash.delete(:TORQUEBOX_SYMBOL_KEYS)
        hash.delete_if do |key,value|
          if ( Symbol === key )
            key = key.to_s
            symbol_keys << key.to_s
          end
          if ( String === key )
            case value
              when String, Numeric, true, false, nil
                session.setAttribute( key, value )
                true
            else
              if value.respond_to?(:java_object)
                session.setAttribute( key, value )
                true
              else
                false
              end
            end
          end
        end
        session.setAttribute( SYMBOL_KEYS, symbol_keys.to_java )
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


