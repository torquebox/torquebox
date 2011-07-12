class ApplicationController < ActionController::Base
  include TorqueBox::Injectors
  
  protect_from_forgery

  def thing_one
      inject( org.torquebox.ThingOne )
  end
end
