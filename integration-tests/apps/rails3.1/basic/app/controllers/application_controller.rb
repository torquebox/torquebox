class ApplicationController < ActionController::Base

  protect_from_forgery

  def thing_one
      TorqueBox.fetch( org.torquebox.ThingOne )
  end
end
