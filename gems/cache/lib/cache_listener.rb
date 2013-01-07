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

require 'jruby/core_ext'

module TorqueBox
  module Infinispan
    class CacheListener

      def event_fired( event )
        event_type = event.get_type.to_s.downcase
        if respond_to? event_type
          self.send( event_type, event )
        else
          puts "#{self.class.name}##{event_type} not implemented."
        end
      end

      if JRUBY_VERSION =~ /^1\.7/
        add_class_annotations( { org.infinispan.notifications.Listener => { } } )
      else
        add_class_annotation( { org.infinispan.notifications.Listener => { } } )
      end
      add_method_signature( "event_fired", [java.lang.Void::TYPE, org.infinispan.notifications.cachelistener.event.Event] )
      add_method_annotation( "event_fired", 
                            { org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated    => {},
                              org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved    => {},
                              org.infinispan.notifications.cachelistener.annotation.CacheEntryModified   => {},
                              org.infinispan.notifications.cachelistener.annotation.CacheEntryEvicted    => {},
                              org.infinispan.notifications.cachelistener.annotation.CacheEntryActivated  => {},
                              org.infinispan.notifications.cachelistener.annotation.CacheEntryEvicted    => {},
                              org.infinispan.notifications.cachelistener.annotation.CacheEntryVisited    => {}})
      become_java!
    end
  end
end


