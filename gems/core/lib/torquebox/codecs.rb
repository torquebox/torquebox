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
  module Codecs
    class << self

      def [](key)
        case key
        when :edn
          require 'torquebox/codecs/edn' unless defined?(TorqueBox::Codecs::EDN)
          TorqueBox::Codecs::EDN
        when :json
          require 'torquebox/codecs/json' unless defined?(TorqueBox::Codecs::JSON)
          TorqueBox::Codecs::JSON
        when :marshal
          require 'torquebox/codecs/marshal' unless defined?(TorqueBox::Codecs::Marshal)
          TorqueBox::Codecs::Marshal
        when :marshal_base64
          require 'torquebox/codecs/marshal_base64' unless defined?(MarshalBase64)
          MarshalBase64
        when :marshal_smart
          require 'torquebox/codecs/marshal_smart' unless defined?(MarshalSmart)
          MarshalSmart
        else
          raise "Unsupported codec #{key}"
        end
      end
      
      def encode(data, encoding)
        self[encoding].encode(data)
      end

      def decode(data, encoding)
        self[encoding].decode(data)
      end
      
    end
  end
end
