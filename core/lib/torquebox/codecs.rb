# Copyright 2014 Red Hat, Inc, and individual contributors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

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
