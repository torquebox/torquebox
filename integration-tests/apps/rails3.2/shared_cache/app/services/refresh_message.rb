require 'torquebox-cache'
    
class RefreshMessage
  attr_accessor :persisted_cache, :memory_cache, :should_run

  def initialize(opts = {})
    @persisted_cache = TorqueBox::Infinispan::Cache.new( :name => 'persisted_cache', :persist => true, :encoding => :marshal_base64 )
    @memory_cache    = TorqueBox::Infinispan::Cache.new( :name => 'memory_cache', :encoding => :marshal_base64 )
  end

  def start
    persisted_cache.put( "time", Time.now )
    memory_cache.put( "time", Time.now )
  end

  def stop
    puts "Service stopped"
  end
  
end
