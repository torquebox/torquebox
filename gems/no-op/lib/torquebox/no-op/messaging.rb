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
    module Backgroundable
      def self.included(base)
        base.extend(ClassMethods)
      end

      def background(options = {})
        self
      end

      module ClassMethods
        def always_background(*methods)
          # no-op
        end
      end
    end

    class MessageProcessor
    end

    class Queue
      def initialize(*args)
        # no-op
      end

      def publish(*args)
        # no-op
      end

      def receive(*args)
        # no-op
      end
    end

    class Message
      class <<self
        def register_encoding(klass)
          # no-op
        end
      end
    end
  end
end
