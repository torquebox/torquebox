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

require 'base64'

java_import org.projectodd.wunderboss.codecs.StringCodec

module TorqueBox
  module Codecs
    class MarshalBase64 < StringCodec

      def initialize
        super("marshal_base64", "application/ruby-marshal-base64")
      end

      def encode(data)
        Base64.encode64(::Marshal.dump(data)) unless data.nil?
      end

      def decode(data)
        ::Marshal.restore(Base64.decode64(data)) unless data.nil?
      end

    end
  end
end
