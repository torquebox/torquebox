# Be sure to restart your server when you modify this file.

Basic::Application.config.session_store TorqueBox::Session::ServletStore

# Use the database for sessions instead of the cookie-based default,
# which shouldn't be used to store highly confidential information
# (create the session table with "rake db:sessions:create")
# Basic::Application.config.session_store :active_record_store
