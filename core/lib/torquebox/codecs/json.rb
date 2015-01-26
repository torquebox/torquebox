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

java_import org.projectodd.wunderboss.codecs.StringCodec

module TorqueBox
  module Codecs
    class JSON < StringCodec

      def initialize
        super("json", "application/json")
      end

      # @api private
      def require_json
        # We can't ship our own json, as it may collide with the gem
        # requirement for the app.
        unless defined?(::JSON)
          begin
            require 'json'
          rescue LoadError
            raise RuntimeError.new("Unable to load the json gem. Verify that "\
                                   "is installed and in your Gemfile (if using Bundler)")
          end
        end
      end

      def encode(data)
        require_json
        begin
          if data.respond_to?(:as_json)
            data = data.as_json
          end
          ::JSON.fast_generate(data) unless data.nil?
        rescue ::JSON::GeneratorError
          ::JSON.dump(data)
        end
      end

      def decode(data)
        require_json
        begin
          ::JSON.parse(data, :symbolize_names => true) unless data.nil?
        rescue ::JSON::ParserError
          ::JSON.load(data)
        end
      end

    end
  end
end
