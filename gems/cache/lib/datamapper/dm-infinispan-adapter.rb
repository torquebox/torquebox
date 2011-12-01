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
require 'torquebox-cache' # is this needed?
require 'datamapper/model'
require 'datamapper/search'


module DataMapper::Adapters

  class InfinispanAdapter < AbstractAdapter

    include TorqueBox::Infinispan

    DataMapper::Model.append_inclusions( Infinispan::Model )

    def initialize( name, options )
      super
      @options            = options.dup
      @metadata           = @options.dup
      @options[:name]     = name.to_s
      @options[:index]    = true
      @metadata[:name]    = name.to_s + "/metadata"
      @cache              = Cache.new( @options )
      @metadata_cache     = Cache.new( @metadata )
      @search             = Infinispan::Search.new(cache, lambda{ |v| self.deserialize(v) })
    end


    def create( resources )
      cache.transaction do
        resources.each do |resource|
          initialize_serial( resource, @metadata_cache.increment( index_for( resource ) ) )
          cache.put( key( resource ), serialize( resource ) ) 
        end
      end
    end

    def read( query )
      query.filter_records(@search.search( query ))
    end

    def update( attributes, collection )
      attributes = attributes_as_fields(attributes)
      cache.transaction do
        collection.each do |resource|
          resource.attributes(:field).merge(attributes)
          cache.put( key(resource), serialize(resource) )
        end
      end
    end

    def delete( collection )
      cache.transaction do
        collection.each do |resource|
          cache.remove( key(resource) )
        end
      end
    end

    def stop
      cache.stop
    end

    def serialize(resource)
      resource.is_a?(DataMapper::Resource) ? resource : resource.to_json
    end

    def deserialize(value)
      value.is_a?(String) ? JSON.parse(value) : value
    end

    def search_manager
      @search.search_manager
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
    
    def index_for( resource )
      resource.model.name + ".index"
    end

    def all_records
      records = []
      cache.keys.each do |key|
        value = cache.get(key)
        records << deserialize(value) if value
      end
      records
    end

  end
end

