
class RootController < ApplicationController


  def index
  end

  # Be sure we're using torquebox cache
  def torqueboxey
    @cache_type = Rails.cache.class.name
    @cache_mode = Rails.cache.clustering_mode
  end

  def cachey
    Rails.cache.write( "taco", "crunchy" )
    @cache_value = Rails.cache.read( "taco" )
  end

  def cacheytx
    TorqueBox.transaction do
      Rails.cache.write( "taco", "crunchy" )
    end
    @cache_value = Rails.cache.read( "taco" )
    render "root/cachey"
  end

  def cacheytxthrows
    Rails.cache.write( "taco", "soft" )
    begin
      TorqueBox.transaction do
        Rails.cache.write( "taco", "crunchy" )
        raise "I like it soft"
      end
    rescue Exception => e
      # Exception should be "I like it soft"
    end
    @cache_value = Rails.cache.read( "taco" )
    render "root/cachey"
  end

  # The Rails.cache is using ActiveSupport::Cache::TorqueBoxStore which
  # defaults to invalidation mode. That mode does not replicate or
  # distribute values across nodes. So, we'll use an alacarte cache
  # to test clustered values

  # Clustered tests
  def clustery
    cache = TorqueBox::Infinispan::Cache.new
    @cache_type = cache.class.name
    @cache_mode = cache.clustering_mode
    render "root/torqueboxey"
  end

  def putcache
    cache = TorqueBox::Infinispan::Cache.new
    cache.put( "mode", "clustery" )
    @cache_value = cache.get( "mode" )
    render "root/cachey"
  end

  def getcache
    cache = TorqueBox::Infinispan::Cache.new
    cache.put( "mode", "clustery" )
    @cache_value = cache.get( "mode" )
    render "root/cachey"
  end

  def writecache
    cache = ActiveSupport::Cache::TorqueBoxStore.new(:mode=>:dist, :name=>'distributed_cache_test')
    cache.write( "mode", "clustery" )
    @cache_value = cache.read( "mode" )
    render "root/cachey"
  end

  def readcache
    cache = ActiveSupport::Cache::TorqueBoxStore.new(:mode=>:dist, :name=>'distributed_cache_test')
    @cache_value = cache.read( "mode" )
    render "root/cachey"
  end

end
