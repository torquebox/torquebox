# Load the rails application
require File.expand_path('../application', __FILE__)

# Initialize the rails application
Basic::Application.initialize!

require 'torquebox-cache' # triggers TORQUE-635 error
Basic::Application.config.cache_store = :torquebox_store
