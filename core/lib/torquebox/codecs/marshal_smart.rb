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
    class MarshalSmart < StringCodec

      def initialize
        super("marshal_smart", "application/ruby-marshal-smart")
      end

      # @api private
      MARSHAL_MARKER = "_|marshalled|_"

      def encode(object)
        case object
        when String, Numeric, true, false, nil
          object
        else
          if object.respond_to?(:java_object)
            object
          else
            MARSHAL_MARKER + Base64.encode64(::Marshal.dump(object))
          end
        end
      end

      def decode(object)
        if object.is_a?(String) && object.start_with?(MARSHAL_MARKER)
          object = ::Marshal.load(Base64.decode64(object.sub(MARSHAL_MARKER, '')))
        end
        object
      end

    end
  end
end
