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
      @cache  = TorqueBox::Infinispan::Cache.new( opts )
      @models = TorqueBox::Infinispan::Cache.new( :name => name.to_s + "/models" )
    end


    def create( resources )
      resources.each do |resource|
        initialize_serial( resource, increment(resource) )
        @cache.put( key( resource ), serialize( resource ) )
      end
    end

    def read( query )
      # TODO: This is not really acceptable at all
      records = []
      @cache.keys.each do |key|
        value = @cache.get(key)
        records << deserialize(value) if value
      end
      records = query.filter_records(records)
      records
    end

    def update( attributes, collection )
      attributes = attributes_as_fields(attributes)
      collection.each do |resource|
        resource.attributes(:field).merge(attributes)
        @cache.put( key(resource), serialize(resource) )
      end
    end

    def delete( collection )
      collection.each do |resource|
        @cache.remove( key(resource) )
      end
    end

    private
    def next_id(resource)
      Digest::SHA1.hexdigest(Time.now.to_i + rand(1000000000).to_s)[1..length].to_i
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
      return JSON.parse(string) 
    end

    def increment(resource, amount = 1)
      key = resource.model.name + ".index"
      current = @models.get( key )
      @models.put(key, amount) and return amount if current.nil?
      new_value = current+amount
      if @models.replace( key, current, new_value )
        return new_value
      else
        raise "Concurrent modification, old value was #{value} new value #{new_value}"
      end
    end

    # Decrement an integer value in the cache; return new value
    def decrement(name, amount = 1)
      increment( name, -amount )
    end
  end
end

