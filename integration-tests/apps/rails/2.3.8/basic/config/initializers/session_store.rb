# Be sure to restart your server when you modify this file.

# Your secret key for verifying cookie session data integrity.
# If you change this key, all old sessions will become invalid!
# Make sure the secret is at least 30 characters and all random, 
# no regular words or you'll be exposed to dictionary attacks.
#ActionController::Base.session = {
  #:key         => '_basic-rails_session',
  #:secret      => '3d125d5fd5bc312a40804d6373e0f6fef5b4ffdd1a3366276221b96cee36e31c240da04c7382a2cd90e165a95115e00838c76e3718ecb3847a1095362cb7f5de'
#}

# Use the database for sessions instead of the cookie-based default,
# which shouldn't be used to store highly confidential information
# (create the session table with "rake db:sessions:create")
# ActionController::Base.session_store = :active_record_store

( ActionController::Base.session_store = TorqueBox::Session::ServletStore ) if defined?(TorqueBox::Session::ServletStore)
