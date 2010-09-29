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

require 'org/torquebox/rails/web/v2_2/servlet_session'

module TorqueBox
  module Rails
    class ServletSessionManager
      RAILS_SESSION_KEY = "__current_rails_session"
      
      def initialize(session, option) 
        @servlet_request = option['servlet_request']
        @digest = 'SHA1'
        java_session = @servlet_request.getSession()
        if ( java_session )
            @session_id   = java_session.getId()
        end
        @session_data = {}
        @session_data['jboss.rails.initialized']=true
        update
      end
      
      def session_id
        @session_id 
      end
      
      def restore
        @session_data = {}
        @session_id   = nil
        java_session = @servlet_request.getSession()
        if java_session
          @session_id = java_session.getId()
          java_session.getAttributeNames.each do |k|
            if k == RAILS_SESSION_KEY
              marshalled_bytes = java_session.getAttribute(RAILS_SESSION_KEY)
              if marshalled_bytes
                data = Marshal.load(String.from_java_bytes(marshalled_bytes))
                @session_data.update data if Hash === data
              end
            else
              @session_data[k] = java_session.getAttribute(k)
            end
          end
        end
        @session_data
      end
      
      def update
        java_session = @servlet_request.getSession(true)
        hash = @session_data.dup
        hash.delete_if do |k,v|
          if String === k
            case v
              when String, Numeric, true, false, nil
              java_session.setAttribute k, v
              true
            else
              if v.respond_to?(:java_object)
                java_session.setAttribute k, v
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
          java_session.setAttribute(RAILS_SESSION_KEY, marshalled_bytes)
        end
      end
      
      # Update and close the Java session entry
      def close
        update
      end
      
      # Delete the Java session entry
      def delete
        java_session = @servlet_request.getSession(false)
        java_session.invalidate if java_session
      end
      
      def generate_digest(data)
        java_session = @servlet_request.getSession(true)
        @secret ||= java_session.getAttribute("__rails_secret")
        unless @secret
          @secret = java_session.getId + java_session.getLastAccessedTime.to_s
          java_session.setAttribute("__rails_secret", @secret)
        end
        OpenSSL::HMAC.hexdigest(OpenSSL::Digest::Digest.new(@digest), @secret, data)
      end
      
      # The session state
      def data
        @session_data
      end
      
      def []=(k, v)
        @session_data[k] = v
      end
      
      def [](k)
        @session_data[k]
      end
      
      def each(&b)
        @session_data.each(&b)
      end
      
      private
      # Attempts to redirect any messages to the data object.
      def method_missing(name, *args, &block)
        @session_data.send(name, *args, &block)
      end
      
    end
  end
end
