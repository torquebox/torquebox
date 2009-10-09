
require 'rubygems'
require 'vfs'
require 'pp'

class Class
  
  alias_method :method_added_before_torquebox, :method_added
  
  def method_added(method_name)
    recursing = caller.find{|e| e =~ /org.torquebox.rails.runtime.deployers.*method_added/ }
    unless ( recursing ) 
      if ( (self.to_s == 'Rails::Configuration') && ( method_name == :set_root_path! ) )
        self.class_eval do
          def set_root_path!
            @root_path = RAILS_ROOT
          end 
        end
      end
      if ( (self.to_s == 'Rails::Initializer') && ( method_name == :load_application_initializers ) )
     	  self.class_eval do
          alias_method :load_application_initializers_before_torquebox, :load_application_initializers      	    
          def load_application_initializers()
            puts "about to load application initializers"
            load_application_initializers_before_torquebox()              
            puts "completed to load application initializers"
            def (ActionController::SessionManagement::ClassMethods).raw_session_store()
              return class_variable_get( :@@session_store ) if class_variable_defined?( :@@session_store )
              nil
            end
            puts "raw_session_store=#{ActionController::SessionManagement::ClassMethods.raw_session_store}"
            if ( ActionController::SessionManagement::ClassMethods.raw_session_store.nil? )
              puts "Setting to servlet store"
              require 'org/torquebox/rails/web/v2_3/servlet_session'
              ActionController::Base.session_store = JBoss::Session::Servlet
            else
              puts "Using session store #{ActionController::Base.session_store}"
            end
       	  end
        end
      end
      method_added_before_torquebox(method_name)
    end # unless ( recursing )
  end # method_added
end

load RAILS_ROOT + '/config/environment.rb'
