require 'torquebox-cache'
    
class RefreshMessage
  attr_accessor :persisted_cache, :memory_cache, :should_run

  def initialize(opts = {})
    @persisted_cache = TorqueBox::Infinispan::Cache.new( :name => 'persisted_cache', :persist => true )
    @memory_cache    = TorqueBox::Infinispan::Cache.new( :name => 'memory_cache' )
    @should_run      = true
  end

  def start
    Thread.new { run }
  end

  def stop
    should_run = false
  end
  
  def run
    while( should_run ) do
      persisted_cache.put( "time", Time.now )
      memory_cache.put( "time", Time.now )
      sleep 1
    end
  end
  
end
