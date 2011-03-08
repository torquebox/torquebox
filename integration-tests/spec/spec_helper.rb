require 'container'
require 'capybara/dsl'

Capybara.register_driver :selenium do |app|
  Capybara::Driver::Selenium.new(app, :browser => :firefox)
end

Capybara.default_driver = :selenium
Capybara.app_host = "http://localhost:8080"
Capybara.run_server = false

RSpec.configure do |config|
  config.include Capybara
  config.after do
    Capybara.reset_sessions!
  end
end

MUTABLE_APP_BASE_PATH  = File.join( File.dirname( __FILE__ ), '..', 'target', 'apps' )

def rewrite_file(file_name, replace, replacement)
  lines = File.readlines( file_name )
  File.open( file_name, 'w' ) do |f|
    lines.each do |line|
      f.write( line.gsub( replace, replacement ) )
    end
  end
end


