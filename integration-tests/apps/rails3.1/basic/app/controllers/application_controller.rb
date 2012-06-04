class ApplicationController < ActionController::Base
  include TorqueBox::Injectors
  
  protect_from_forgery

  def thing_one
      fetch( org.torquebox.ThingOne )
  end
end
