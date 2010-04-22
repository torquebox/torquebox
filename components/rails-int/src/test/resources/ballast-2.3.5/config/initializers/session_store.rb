# Be sure to restart your server when you modify this file.

# Your secret key for verifying cookie session data integrity.
# If you change this key, all old sessions will become invalid!
# Make sure the secret is at least 30 characters and all random, 
# no regular words or you'll be exposed to dictionary attacks.
ActionController::Base.session = {
  :key         => '_ballast-2.3.5_session',
  :secret      => 'ab3fb6ce1b09693a913fcac8b077c326b75183a75b2453cc6eb4675068d14a4532c7385e78e4cc650cd2c44e3f670fc3181e05d78e513526e39aaaa33dceda57'
}

# Use the database for sessions instead of the cookie-based default,
# which shouldn't be used to store highly confidential information
# (create the session table with "rake db:sessions:create")
# ActionController::Base.session_store = :active_record_store
