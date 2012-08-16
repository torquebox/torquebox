class RootController < ApplicationController

  caches_page :page_caching

  def page_caching
    @time = params[:time]
  end

  def expire_page_cache
    format = request.format.json? ? 'json' : 'html'
    expire_page :action => :page_caching, :format => format
    render :index
  end

end
