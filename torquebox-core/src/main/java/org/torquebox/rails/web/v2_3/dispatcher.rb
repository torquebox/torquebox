puts "loading servlet_session"
require 'org/torquebox/rails/web/v2_3/servlet_session'

puts "loading TorqueBox Dispatcher"
module TorqueBox
  module Rails
    module V2_3
      module Rack
        class Dispatcher < ActionController::Dispatcher
          def initialize(context)
            super()
            ActionController::Base.relative_url_root = context
            ActionController::Base.session_store     = JBoss::Session::Servlet
          end          
        end 
      end
    end  
  end  
end