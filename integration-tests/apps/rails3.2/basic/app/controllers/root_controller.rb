class RootController < ApplicationController

  caches_page :page_caching

  def page_caching
    @time = params[:time]
  end

  def expire_page_cache
    format = request.format.json? ? 'json' : 'html'
    expire_page :action => :page_caching, :format => format
    @time = Time.now.to_f
    render :page_caching
  end

  def multiple_cookies
    cookies['foo1'] = 'bar1'
    cookies['foo2'] = { :value => 'bar2', :expires => 1.hour.from_now }
    cookies['foo3'] = 'bar3'
    render :text => "something\n"
  end

end
