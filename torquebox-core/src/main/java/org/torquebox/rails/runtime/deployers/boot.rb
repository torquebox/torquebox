
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
      end
      method_added_before_torquebox(method_name)
    end # unless ( recursing )
  end # method_added
end

load RAILS_ROOT + '/config/environment.rb'
