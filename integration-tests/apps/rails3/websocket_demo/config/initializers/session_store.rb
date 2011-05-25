# Be sure to restart your server when you modify this file.

# WebsocketDemo::Application.config.session_store :cookie_store, :key => '_websocket_demo_session'
WebsocketDemo::Application.config.session_store TorqueBox::Session::ServletStore if defined?(TorqueBox::Session::ServletStore)

# Use the database for sessions instead of the cookie-based default,
# which shouldn't be used to store highly confidential information
# (create the session table with "rails generate session_migration")
# WebsocketDemo::Application.config.session_store :active_record_store

