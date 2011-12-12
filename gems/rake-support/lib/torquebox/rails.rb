require 'rails/generators'
require 'rails/generators/rails/app/app_generator'

module TorqueBox
  class Rails
    def self.new_app
      # Assumes ARGV[0] already has the application name
      ARGV << [ "-m", "#{ENV['TORQUEBOX_HOME']}/share/rails/template.rb" ]
      ARGV.flatten!
      ::Rails::Generators::AppGenerator.start
    end
  end
end
