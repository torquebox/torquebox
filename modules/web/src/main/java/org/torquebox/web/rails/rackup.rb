
require %q(org/torquebox/web/rails/v2_3/rackup_generator)
generator = TorqueBox::Rails::V2_3::Rack::Generator

module TorqueBox
  module Rails
    def self.app=(app)
      @app = app
    end

    def self.app
      @app
    end
  end
end
TorqueBox::Rails.app = generator.generate( TORQUEBOX_RACKUP_CONTEXT )
