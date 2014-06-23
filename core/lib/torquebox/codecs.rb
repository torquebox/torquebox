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

java_import org.projectodd.wunderboss.codecs.Codecs
java_import org.projectodd.wunderboss.codecs.None

module TorqueBox
  module Codecs
    class << self

      def add(codec)
        java_codecs.add(codec)
        self
      end

      def [](key)
        java_codecs.for_content_type(key.to_s) ||
          java_codecs.for_name(key.to_s) ||
          fail("Unsupported codec #{key}")
      end

      def encode(data, encoding)
        self[encoding].encode(data)
      end

      def decode(data, encoding)
        self[encoding].decode(data)
      end

      def java_codecs
        @codecs ||= org.projectodd.wunderboss.codecs.Codecs.new
      end
    end
  end
end

TorqueBox::Codecs.add(TorqueBox::Codecs::EDN.new)
TorqueBox::Codecs.add(TorqueBox::Codecs::JSON.new)
TorqueBox::Codecs.add(TorqueBox::Codecs::Marshal.new)
TorqueBox::Codecs.add(TorqueBox::Codecs::MarshalBase64.new)
TorqueBox::Codecs.add(TorqueBox::Codecs::MarshalSmart.new)
TorqueBox::Codecs.add(TorqueBox::Codecs::Text.new)
