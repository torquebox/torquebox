
class RootController < ApplicationController

  def index
  end

  # Be sure we're using torquebox cache
  def torqueboxey
    if params[:mode]
      cache = storecache(params[:mode], params[:sync])
    else
      cache = Rails.cache
    end
    @cache_type = cache.class.name
    @cache_mode = cache.clustering_mode
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

  def eviction
    evictedcache.put('a', 'z')
    evictedcache.put('b', 'y')
    evictedcache.put('c', 'x')
    @cache_value = evictedcache.size
    render "root/cachey"
  end


  # The Rails.cache is using ActiveSupport::Cache::TorqueBoxStore which
  # defaults to invalidation mode. That mode does not replicate or
  # distribute values across nodes. So, we'll use an alacarte cache
  # to test clustered values

  # Clustered tests
  def clustery
    @cache_type = defaultcache.class.name
    @cache_mode = defaultcache.clustering_mode
    render "root/torqueboxey"
  end

  def putcache
    key = params['symbol'] ? :mode : 'mode'
    defaultcache.put( key, { :value => "clustery" } )
    @cache_value = defaultcache.get( key )[:value]
    render "root/cachey"
  end

  def getcache
    key = params['symbol'] ? :mode : 'mode'
    @cache_value = defaultcache.get( key )[:value]
    render "root/cachey"
  end

  def writecache
    cache = storecache(params[:mode], params[:sync])
    cache.write( "mode", "clustery" )
    @cache_value = cache.read( "mode" )
    render "root/cachey"
  end

  def readcache
    cache = storecache(params[:mode], params[:sync])
    @cache_value = cache.read( "mode" )
    render "root/cachey"
  end

  def putrepl
    replcache.put( "mode", "clustery" )
    @cache_value = replcache.get( "mode" )
    render "root/cachey"
  end

  def getrepl
    @cache_value = replcache.get( "mode" )
    render "root/cachey"
  end

  def putprocessor
    # causes the processor to write to the cache
    queue = TorqueBox.fetch( '/queue/simple_queue' )
    message = { :action => "write", :message => "clustery" } 
    queue.publish( message )

    # wait until the processor has spun up and placed the message in
    # the cache
    queue = TorqueBox.fetch( '/queue/backchannel' )
    queue.receive( :timeout => 30000 )

    @cache_value = "success"
    render "root/cachey"
  end

  def getprocessor
    # cause the processor to read from the cache 
    # and publish the value to backchannel
    queue = TorqueBox.fetch( '/queue/simple_queue' )
    message = { :action => "read" }
    queue.publish( message )

    queue = TorqueBox.fetch( '/queue/backchannel' )
    @cache_value = queue.receive( :timeout => 30000 )
    render "root/cachey"
  end

  protected
  def defaultcache
    @defaultcache ||= TorqueBox::Infinispan::Cache.new
  end

  def replcache
    @replcache ||= TorqueBox::Infinispan::Cache.new(:name=>'testrepl', :mode=>:repl)
  end

  def storecache(mode, sync)
    sync = sync == 'true'
    @storecache ||= ActiveSupport::Cache::TorqueBoxStore.new(:mode=>mode.to_sym,
                                                             :sync=>sync,
                                                             :name=>"#{mode}_#{sync}_cache_test")
  end

  def evictedcache
    @evictedcache ||= TorqueBox::Infinispan::Cache.new(:name=>"evicted_test", 
                                                       :max_entries=>2)
  end
end
