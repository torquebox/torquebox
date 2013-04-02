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
    module JSON
      class << self

        def require_json
          # We can't ship our own json, as it may collide with the gem
          # requirement for the app.
          if !defined?( ::JSON )
            begin
              require 'json'
            rescue LoadError => ex
              raise RuntimeError.new( "Unable to load the json gem. Verify that is installed and in your Gemfile (if using Bundler)" )
            end
          end
        end
        
        def encode(data)
          require_json
          begin
            if ( data.respond_to?( :as_json ) )
              data = data.as_json
            end
            ::JSON.fast_generate( data ) unless data.nil?
          rescue ::JSON::GeneratorError
            ::JSON.dump(data)
          end
        end

        def decode(data)
          require_json
          begin
            ::JSON.parse( data, :symbolize_names => true ) unless data.nil?
          rescue ::JSON::ParserError
            ::JSON.load(data)
          end
        end

      end
    end
  end
end
