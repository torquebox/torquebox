# Configure the TorqueBox Servlet-based session store.
# Provides for server-based, in-memory, cluster-compatible sessions
Twitter::Application.config.session_store :torquebox_store
