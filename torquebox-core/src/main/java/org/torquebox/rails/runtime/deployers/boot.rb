
require 'rubygems'
require 'vfs'
require 'pp'

class Class
  
  alias_method :method_added_before_torquebox, :method_added
  
  def method_added(method_name)
    recursing = caller.find{|e| e =~ /org.torquebox.rails.runtime.deployers.*method_added/ }
    unless ( recursing ) 
      case self.to_s
      when 'Rails::Configuration' 
        case( method_name )
       	when :set_root_path!
          self.class_eval do
            def set_root_path!
              @root_path = RAILS_ROOT
            end 
          end
        end
      when 'Rails::Initializer' 
        case( method_name )
       	when :load_application_initializers
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
      end
      method_added_before_torquebox(method_name)
    end # unless ( recursing )
  end # method_added
end

load RAILS_ROOT + '/config/environment.rb'
