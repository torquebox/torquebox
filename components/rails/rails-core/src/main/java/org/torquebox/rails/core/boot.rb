
require 'rubygems'
require 'vfs'
require 'pp'

class Class
  
  alias_method :method_added_before_torquebox, :method_added
  
  def method_added(method_name)
    recursing = caller.find{|e| e =~ /org.torquebox.rails.*method_added/ }
    unless ( recursing ) 
      if ( (self.to_s == 'Rails::Configuration') && ( method_name == :set_root_path! ) )
        self.class_eval do
          def set_root_path!
            #puts "set_root_path! to #{RAILS_ROOT}"
            @root_path = RAILS_ROOT
          end 
        end
      end
      if ( (self.to_s == 'Rails::Initializer') && ( method_name == :load_application_initializers ) )
     	  self.class_eval do
          alias_method :load_application_initializers_before_torquebox, :load_application_initializers      	    
          def load_application_initializers()
            load_application_initializers_before_torquebox()              
            def (ActionController::SessionManagement::ClassMethods).raw_session_store()
              return class_variable_get( :@@session_store ) if class_variable_defined?( :@@session_store )
              nil
            end
            if ( ActionController::SessionManagement::ClassMethods.raw_session_store.nil? )
              require 'org/torquebox/rails/web/v2_3/servlet_session'
              ActionController::Base.session_store = JBoss::Session::Servlet
            end
       	  end
        end
      end
      if ( (self.to_s == 'Rails::Initializer') && ( method_name == :set_autoload_paths ) )
        self.class_eval do
          alias_method :set_autoload_paths_before_torquebox, :set_autoload_paths            
          def set_autoload_paths
            configuration.load_paths += TORQUEBOX_RAILS_AUTOLOAD_PATHS.to_a
            set_autoload_paths_before_torquebox
          end
        end
      end
      method_added_before_torquebox(method_name)
    end # unless ( recursing )
  end # method_added
end

begin
  load RAILS_ROOT + '/config/environment.rb'
rescue => e
  puts e.message
  puts e.backtrace
  puts ""
  puts ""
  puts ""
  raise e
end
