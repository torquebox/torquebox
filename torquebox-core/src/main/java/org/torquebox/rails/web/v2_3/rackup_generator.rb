
#require 'org/torquebox/rails/web/v2_3/rails_rack_dispatcher'
require 'org/torquebox/rails/web/v2_3/dispatcher'

ActionController::Base.relative_url_root = TORQUEBOX_RACKUP_CONTEXT

module TorqueBox
  module Rails
    module V2_3
      module Rack
        module Generator
          def self.generate(context)
            ::Rack::Builder.new {
              run ActionController::Dispatcher.new
            }.to_app
          end
        end
      end
    end
  end
end