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

java_import org.projectodd.wunderboss.codecs.BytesCodec

module TorqueBox
  module Codecs
    class Marshal < BytesCodec

      def initialize
        super("marshal", "application/ruby-marshal")
      end

      def encode(data)
        ::Marshal.dump(data).to_java_bytes unless data.nil?
      end

      def decode(data)
        ::Marshal.restore(String.from_java_bytes(data)) unless data.nil?
      end

    end
  end
end
