
#require 'org/torquebox/rails/web/v2_3/rails_rack_dispatcher'
require 'org/torquebox/rails/web/v2_3/dispatcher'

module TorqueBox
  module Rails
    module V2_3
      module Rack
        module Generator
          def self.generate(context)
            const_set( 'RELATIVE_URL_ROOT', context )
            
            ::Rack::Builder.new {
              run TorqueBox::Rails::V2_3::Rack::Dispatcher.new( context )
            }.to_app
          end
        end
      end
    end
  end
end