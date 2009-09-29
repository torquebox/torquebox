
if ( Rails::VERSION::MAJOR == 2 )
  case ( Rails::VERSION::MINOR ) 
      when 2
        puts "Racking up a rails 2.2.x application"
        require %q(org/torquebox/rails/web/v2_2/rackup_generator)
        generator = TorqueBox::Rails::V2_2::Rack::Generator
      when 3
        puts "Racking up a rails 2.3.x application"
        require %q(org/torquebox/rails/web/v2_3/rackup_generator)
        generator = TorqueBox::Rails::V2_3::Rack::Generator
  end
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
puts "TorqueBox::Rails.app=#{TorqueBox::Rails.app.inspect}"