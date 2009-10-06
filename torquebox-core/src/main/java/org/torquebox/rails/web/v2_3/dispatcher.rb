require 'action_controller/dispatcher'

module TorqueBox
  module Rails
    module V2_3
      module Rack
        class Dispatcher < ActionController::Dispatcher
          def initialize(context)
            super()
            ActionController::Base.relative_url_root = context
          end          
        end 
      end
    end  
  end  
end