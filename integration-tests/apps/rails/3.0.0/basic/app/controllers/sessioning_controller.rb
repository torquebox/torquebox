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

  def logout
    reset_session
    render :nothing => true
  end

  def set_from_ruby
    session[:a_fixnum] = 42
    session[:a_string] = "swordfish"
    session[:a_boolean] = true

    redirect_to :action=>:display_session
  end

  def display_session
    @java_session = request.env['java.servlet_request'].session
  end

end
