
puts "Selecting RackUp with #{Rails::VERSION::STRING}"
if ( Rails::VERSION::MAJOR == 2 )
  case ( Rails::VERSION::MINOR ) 
      when 2
        puts "Select v2_2"
        require %q(org/torquebox/rails/web/v2_2/rackup_generator)
        generator = TorqueBox::Rails::V2_2::Rack::Generator
      when 3
        puts "Select v2_3"
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
puts "using generator #{generator}"
TorqueBox::Rails.app = generator.generate( TORQUEBOX_RACKUP_CONTEXT )
puts "created app #{TorqueBox::Rails.app}"