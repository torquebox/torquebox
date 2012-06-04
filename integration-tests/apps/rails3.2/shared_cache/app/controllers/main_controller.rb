class MainController < ApplicationController
  def index
    check_for_success( false )
  end

  def persisted
    check_for_success( true )
  end

  def check_for_success( persisted )
    name = persisted ? "persisted_cache" : "memory_cache"
    cache = TorqueBox::Infinispan::Cache.new( :name => name, :persist => persisted )
    @success = !cache.get( "time" ).nil?
    cache.clear
  end
end
