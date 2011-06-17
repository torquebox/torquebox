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
require "digest/sha1"
require 'dm-core'
require 'cache'
require 'json'

module DataMapper::Adapters

  class InfinispanAdapter < AbstractAdapter

    def initialize( name, options )
      super
      opts = {:name => name}
      opts.merge! options
      @cache = TorqueBox::Infinispan::Cache.new( opts )
    end


    def create( resources )
      resources.each do |resource|
        resource.id = next_id
        key = key( resource )
        @cache.put( key, serialize( resource ) )
      end
    end

    def read( query )
      # TODO: This is not really acceptable at all
      model = query.model
      records = []
      @cache.keys.each do |key|
        value = @cache.get(key)
        records << deserialize(value) if value
      end
      query.filter_records(records)
    end

    def update( attributes, collection )
      attributes = attributes_as_fields(attributes)
      collection.each do |resource|
        attributes = resource.attributes(:field).merge(attributes)
        @cache.put( key(resource), serialize(resource) )
      end
    end

    def delete( collection )
      collection.each do |resource|
        @cache.remove( key(resource) )
      end
    end

    private
    def next_id(length = 10)
      Digest::SHA1.hexdigest(Time.now.to_s + rand(1000000000).to_s)[1..length].to_i
    end

    def key( resource )
      model = resource.model
      key = resource.key.nil? ? '' : resource.key.join('/')
      "#{model}/#{key}/#{resource.id}"
    end      
    
    def serialize(resource_or_attributes)
      if resource_or_attributes.is_a?(DataMapper::Resource)
        resource_or_attributes.attributes(:field)
      else
        resource_or_attributes
      end.to_json
    end

    def deserialize(string)
      JSON.parse(string)
    end
  end

end

