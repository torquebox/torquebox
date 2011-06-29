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
      @options         = options.dup
      @options[:name]  = name.to_s
      @metadata        = @options.dup
      @metadata[:name] = name.to_s + "/metadata"
      @cache           = TorqueBox::Infinispan::Cache.new( @options )
      @metadata_cache  = TorqueBox::Infinispan::Cache.new( @metadata )
    end


    def create( resources )
      resources.each do |resource|
        initialize_serial( resource, @metadata_cache.increment( index_for( resource ) ) )
        cache.put( key( resource ), serialize( resource ) )
      end
    end

    def read( query )
      # TODO: This is not really acceptable at all
      records = []
#      search_manager = org.infinispan.query.Search.getSearchManager(cache.ispan_cache)
      cache.keys.each do |key|
        value = cache.get(key)
        records << deserialize(value) if value
      end
      records = query.filter_records(records)
      records
    end

    def update( attributes, collection )
      attributes = attributes_as_fields(attributes)
      collection.each do |resource|
        resource.attributes(:field).merge(attributes)
        cache.put( key(resource), serialize(resource) )
      end
    end

    def delete( collection )
      collection.each do |resource|
        cache.remove( key(resource) )
      end
    end

    private
    def cache
      @cache
    end

    def metadata_cache
      @metadata_cache
    end

    def next_id(resource)
      Digest::SHA1.hexdigest(Time.now.to_i + rand(1000000000).to_s)[1..length].to_i
    end

    def key( resource )
      model = resource.model
      key = resource.key.nil? ? '' : resource.key.join('/')
      "#{model}/#{key}/#{resource.id}"
    end      
    
    def serialize(resource)
      if resource.is_a?(DataMapper::Resource)
        entry = org.torquebox.web.infinispan.datamapper.Entry.new
        entry.model = resource.model.name
        entry.data  = resource.attributes(:field).to_json
        entry.key   = resource.id.to_s
        entry
      else
        resource.to_json
      end
    end

    def deserialize(value)
      if (value.is_a? String)
        return JSON.parse(value) 
      elsif (value.is_a? org.torquebox.web.infinispan.datamapper.Entry)
        JSON.parse(value.data)
      else
        value
      end
    end

    def index_for( resource )
      resource.model.name + ".index"
    end

  end
end

