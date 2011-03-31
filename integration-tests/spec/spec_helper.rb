require 'container'
require 'capybara/dsl'
require 'akephalos'

Capybara.register_driver :akephalos do |app|
  Capybara::Driver::Akephalos.new(app, :browser => :firefox_3)
end

Capybara.default_driver = :akephalos
Capybara.app_host = "http://localhost:8080"
Capybara.run_server = false

RSpec.configure do |config|
  config.include Capybara
  config.after do
    Capybara.reset_sessions!
  end
end

MUTABLE_APP_BASE_PATH  = File.join( File.dirname( __FILE__ ), '..', 'target', 'apps' )
TESTING_ON_WINDOWS = ( java.lang::System.getProperty( "os.name" ) =~ /windows/i )

def mutable_app(path)
  full_path = File.join( MUTABLE_APP_BASE_PATH, path )
  dest_path = File.dirname( full_path )
  FileUtils.rm_rf( full_path )
  FileUtils.mkdir_p( dest_path )
  FileUtils.cp_r( File.join( File.dirname( __FILE__ ), '..', 'apps', path ), dest_path )
end

def rewrite_file(file_name, replace, replacement)
  lines = File.readlines( file_name )
  File.open( file_name, 'w' ) do |f|
    lines.each do |line|
      f.write( line.gsub( replace, replacement ) )
    end
  end
end



