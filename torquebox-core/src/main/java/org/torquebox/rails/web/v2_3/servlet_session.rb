# JBoss, Home of Professional Open Source
# Copyright 2009, Red Hat Middleware LLC, and individual contributors
# by the @authors tag. See the copyright.txt in the distribution for a
# full listing of individual contributors.
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

module JBoss
  module Session
    class Servlet
      
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
        session_data = {}
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
        session_data[:session_id] = session.getId()
        session_data
      end
      
      def store_session_data(session, session_data)
        hash = session_data.dup
        hash.delete_if do |key,value|
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
        unless hash.empty?
          marshalled_string = Marshal.dump(hash)
          marshalled_bytes = marshalled_string.to_java_bytes
          session.setAttribute(RAILS_SESSION_KEY, marshalled_bytes)
        end
      end
    end
  end
end