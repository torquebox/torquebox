
case ( Rails::VERSION::MAJOR )
when 2
  case ( Rails::VERSION::MINOR ) 
      when 2
        require %q(org/torquebox/rails/web/v2_2/rackup_generator)
        generator = TorqueBox::Rails::V2_2::Rack::Generator
      when 3
        require %q(org/torquebox/rails/web/v2_3/rackup_generator)
        generator = TorqueBox::Rails::V2_3::Rack::Generator
  end
when 3
  require %q(org/torquebox/rails/web/v3_0/rackup_generator)
  generator = TorqueBox::Rails::V3_0::Rack::Generator
end

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