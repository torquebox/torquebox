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

# These codecs don't depend on anything outside Ruby stdlib
require 'torquebox/codecs/marshal'
require 'torquebox/codecs/marshal_base64'
require 'torquebox/codecs/marshal_smart'
require 'torquebox/codecs/text'

# These codecs depend on external gems - attempt to load them
# but ignore any load errors and we'll lazily try again later
require 'torquebox/codecs/json' rescue nil
require 'torquebox/codecs/edn' rescue nil

module TorqueBox
  module Codecs
    class << self

      def register_codec(name, content_type, codec)
        @codecs ||= {}
        @content_types ||= {}
        @codecs[name] = codec
        @content_types[content_type] = name
        @content_types[name] = content_type
      end

      def [](key)
        codec = @codecs[key]
        if codec
          if codec.respond_to?(:call)
            codec.call
          else
            codec
          end
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

      def name_for_content_type(content_type)
        @content_types[content_type]
      end

      alias_method :content_type_for_name, :name_for_content_type

      def binary_content?(encoding)
        codec = self[encoding]
        if codec.respond_to?(:binary_content?)
          codec.binary_content?
        else
          false
        end
      end

    end
  end
end

TorqueBox::Codecs.register_codec(:edn,
                                 'application/edn',
                                 lambda do
                                   # This is only so any issues requiring the edn codec bubble
                                   # up when it gets used
                                   require 'torquebox/codecs/edn' unless defined?(TorqueBox::Codecs::EDN)
                                   TorqueBox::Codecs::EDN
                                 end)

TorqueBox::Codecs.register_codec(:json,
                                 'application/json',
                                 lambda do
                                   # This is only so any issues requiring the json codec bubble
                                   # up when it gets used
                                   require 'torquebox/codecs/json' unless defined?(TorqueBox::Codecs::JSON)
                                   TorqueBox::Codecs::JSON
                                 end)

TorqueBox::Codecs.register_codec(:marshal,
                                 'application/ruby-marshal',
                                 TorqueBox::Codecs::Marshal)

TorqueBox::Codecs.register_codec(:marshal_base64,
                                 'application/ruby-marshal-base64',
                                 TorqueBox::Codecs::MarshalBase64)

TorqueBox::Codecs.register_codec(:marshal_smart,
                                 'application/ruby-marshal-smart',
                                 TorqueBox::Codecs::MarshalSmart)

TorqueBox::Codecs.register_codec(:text,
                                 'text/plain',
                                 TorqueBox::Codecs::Text)
