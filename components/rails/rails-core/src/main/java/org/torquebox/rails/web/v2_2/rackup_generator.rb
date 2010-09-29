
require 'org/torquebox/rails/web/v2_2/rails_rack_dispatcher'

module TorqueBox
  module Rails
    module V2_2
      module Rack
        module Generator
          def self.generate(context)
            ::Rack::Builder.new {
              run TorqueBox::Rails::Rack::Dispatcher.new(context)
            }.to_app      
          end
        end
      end
    end
  end
end
