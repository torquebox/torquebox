#
# Copyright 2011 Red Hat, Inc.
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
#
require 'dm-core'

module DataMapper::Adapters

  class InfinispanAdapter < AbstractAdapter

    def initialize( name, options )
      super
    end


    def create( resources )
      resources.each do |resource|
        resource.id = 1
      end
    end

    def read( query )
      records = [ {:id=>1, :name=>'foo'} ]
      query.filter_records( records )
    end

    def update( attributes, collection )
    end

    def delete( collection )
    end
  end

end

