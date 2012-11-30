# This file is used by Rack-based servers to start the application.

RACKUP_FILE = 'alternate_config.ru'
require ::File.expand_path('../config/environment',  __FILE__)
run Basic::Application
