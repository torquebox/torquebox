
class RootController < ApplicationController

  def index
  end


  def hamltest
  end

  def injectiontest
    puts "About to call thing_one()"
    @use_me = thing_one()
    puts "Called thing_one() -> #{@use_me} #{@use_me.class} #{@use_me.java_class.name}"
  end

  def databaseyml
    @db_user = Rails.application.config.database_configuration["production"]["username"]
  end

  def environment
  end

  caches_page :page_caching

  def page_caching
    @time = params[:time]
  end

  def expire_page_cache
    expire_page :action => :page_caching
    render :index
  end

end
