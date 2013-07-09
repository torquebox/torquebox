# Be sure to restart your server when you modify this file.

if ENV['TORQUEBOX_APP_NAME']
  Basic::Application.config.session_store :torquebox_store
else
  Basic::Application.config.session_store :cookie_store, key: '_basic_session'
end
