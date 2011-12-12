require 'rails/generators'
require 'rails/generators/rails/app/app_generator'

module TorqueBox
  class Rails
    def self.new_app
      # Assumes ARGV[0] already has the application name
      ARGV << [ "-m", TorqueBox::Rails.template ]
      ARGV.flatten!
      ::Rails::Generators::AppGenerator.start
    end

    def self.apply_template( root )
      generator = ::Rails::Generators::AppGenerator.new( [root], {}, :destination_root => root )
      generator.apply TorqueBox::Rails.template 
    end

    def self.template
      "#{ENV['TORQUEBOX_HOME']}/share/rails/template.rb"
    end
  end
end
