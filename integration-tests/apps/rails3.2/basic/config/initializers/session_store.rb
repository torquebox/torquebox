# Configure the TorqueBox Servlet-based session store.
# Provides for server-based, in-memory, cluster-compatible sessions
Basic::Application.config.session_store :torquebox_store
if ENV['TORQUEBOX_APP_NAME']
  Basic::Application.config.session_store :torquebox_store
else
  Basic::Application.config.session_store :cookie_store, :key => '_CHANGEME_session'
end  
