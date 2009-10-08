
require 'rubygems'
require 'vfs'
require 'pp'

class Class
  
  alias_method :method_added_before_torquebox, :method_added
  
  def method_added(method_name)
    recursing = caller.find{|e| e =~ /org.torquebox.rails.runtime.deployers.*method_added/ }
    puts "#{self}##{method_name}"
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
       	when :set_load_path 
          puts "patching set_load_path"
       	  self.class_eval do
       	    alias_method :set_load_path_before_torquebox, :set_load_path
       	    
       	    def set_load_path
              puts "before $LOAD_PATH #{$LOAD_PATH}"
              set_load_path_before_torquebox()
              puts "after $LOAD_PATH #{$LOAD_PATH}"
       	    end
       	  end
       	when :load_application_initializers
          puts "patching load_application_initializers"
       	  self.class_eval do
            alias_method :load_application_initializers_before_torquebox, :load_application_initializers      	    
            def load_application_initializers()
              puts "loading application initializers"
              load_application_initializers_before_torquebox()              
              cls = ActionController::Base
              def cls.raw_session_store()
                puts self.inspect
                puts self.class_variables.inspect
                puts class_variables.inspect
                return class_variable_get( :@@session_store ) if class_variable_defined?( :@@session_store )
                nil
              end
              puts "AAA session_store is #{ActionController::Base.session_store}"
              puts "AAA raw_session_store is #{ActionController::Base.raw_session_store}"
              if ( ActionController::Base.raw_session_store.nil? )
                require 'org/torquebox/rails/web/v2_3/servlet_session'
                ActionController::Base.session_store = JBoss::Session::Servlet
              end
              puts "BBB session_store is #{ActionController::Base.session_store}"
              puts "BBB raw_session_store is #{ActionController::Base.raw_session_store}"
            end
       	  end
        end
      end
      method_added_before_torquebox(method_name)
    end # unless ( recursing )
  end # method_added
end

load RAILS_ROOT + '/config/environment.rb'
