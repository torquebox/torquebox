# Configure the TorqueBox Servlet-based session store.
# Provides for server-based, in-memory, cluster-compatible sessions.
( ActionController::Base.session_store = TorqueBox::Session::ServletStore ) if defined?(TorqueBox::Session::ServletStore)
