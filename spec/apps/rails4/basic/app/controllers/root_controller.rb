class RootController < ApplicationController

  def streaming
    render :stream => true
  end

  def multiple_cookies
    cookies['foo1'] = 'bar1'
    cookies['foo2'] = { :value => 'bar2', :expires => 1.hour.from_now }
    cookies['foo3'] = 'bar3'
    render :text => "something\n"
  end

end
