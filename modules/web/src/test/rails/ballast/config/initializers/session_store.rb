# Be sure to restart your server when you modify this file.

# Your secret key for verifying cookie session data integrity.
# If you change this key, all old sessions will become invalid!
# Make sure the secret is at least 30 characters and all random, 
# no regular words or you'll be exposed to dictionary attacks.
ActionController::Base.session = {
  :key         => '_ballast-2.3.5_session',
  :secret      => 'a7cb3feb62628a7a2c1a4ac3b3c23c49c9fe2fa314b3402a78347691ffe13bc4a7ef69d548ef5c7754c78478c93d3f302c068bc3c0e381ba53665bd33af17370'
}

# Use the database for sessions instead of the cookie-based default,
# which shouldn't be used to store highly confidential information
# (create the session table with "rake db:sessions:create")
# ActionController::Base.session_store = :active_record_store
