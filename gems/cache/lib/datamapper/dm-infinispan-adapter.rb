# Copyright 2008-2012 Red Hat, Inc, and individual contributors.
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

