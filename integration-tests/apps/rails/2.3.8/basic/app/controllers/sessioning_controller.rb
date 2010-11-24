class SessioningController < ApplicationController

  def set_value
    session[:value] = "the value"
    @value = session[:value]
    render :action=>:get_value
  end

  def get_value
    @value = session[:value]
  end

  def clear_value
    session.delete(:value)
    render :action=>:get_value
  end

end
