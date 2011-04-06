# Copyright 2008-2011 Red Hat, Inc, and individual contributors.
# 
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
# 
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public
# License along with this software; if not, write to the Free
# Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA, or see the FSF site: http://www.fsf.org.

require 'pp'

#require 'torquebox-web'

class Class
  
  alias_method :method_added_before_torquebox, :method_added
  
  def method_added(method_name)
    recursing = caller.find{|e| e =~ /org.torquebox.rails.*method_added/ }
    unless ( recursing ) 
      if ( (self.to_s == 'Rails::Configuration') && ( method_name == :set_root_path! ) )
        self.class_eval do
          def set_root_path!
            @root_path = ENV['RAILS_ROOT']
            ::RAILS_ROOT.replace @root_path
          end 
        end
      end
      if ( (self.to_s == 'Rails::Initializer') && ( method_name == :set_autoload_paths ) )
        self.class_eval do
          alias_method :set_autoload_paths_before_torquebox, :set_autoload_paths            
          def set_autoload_paths
            if ( Rails::VERSION::MAJOR == 2 && Rails::VERSION::MINOR == 3 && Rails::VERSION::TINY < 10)
              configuration.load_paths += TORQUEBOX_RAILS_AUTOLOAD_PATHS.to_a
            else
              configuration.autoload_paths += TORQUEBOX_RAILS_AUTOLOAD_PATHS.to_a
            end
            set_autoload_paths_before_torquebox
          end
        end
      end
      if ( (self.to_s == 'Rails::Initializer') && ( method_name == :load_gems ) )
        self.class_eval do
          alias_method :load_gems_before_torquebox, :load_gems            
          def load_gems
            # For frozen Rails 2 applications using ar-jdbc-adapter,
            # it attempts to load rails/railtie.  If this happens to 
            # be available from Rails3 gems, it'll load and then
            # all sorts of nuttiness will occur.  So, let's just hide it.
            if ( Rails::VERSION::MAJOR == 2 )
              unless ( JRuby.runtime.load_service.featureAlreadyLoaded( 'jdbc_adapter/railtie.rb' ) )
                JRuby.runtime.load_service.addLoadedFeature( 'jdbc_adapter/railtie.rb' )
              end
              unless ( JRuby.runtime.load_service.featureAlreadyLoaded( 'arjdbc/jdbc/railtie.rb' ) )
                JRuby.runtime.load_service.addLoadedFeature( 'arjdbc/jdbc/railtie.rb' )
              end
            end
            load_gems_before_torquebox()
            ( require 'action_controller/session/torque_box_store' ) if ( Rails::VERSION::MAJOR == 2 )
          end
        end
      end
      if ( (self.to_s == 'Rails::Application') && ( method_name == :initialize! ) )
        self.class_eval do
          alias_method :initialize_before_torquebox!, :initialize!            
          
          def initialize!
            require 'torquebox-web'
            require 'action_dispatch/session/torque_box_store'
            initialize_before_torquebox!
          end
        end
      end
      method_added_before_torquebox(method_name)
    end # unless ( recursing )
  end # method_added
end


  
begin
  load ENV['RAILS_ROOT'] + '/config/environment.rb'
rescue => e
  puts e.message
  puts e.backtrace
  raise e
end

if ( Rails::VERSION::MAJOR == 2 )
  if ( ActionController::Base.session_store == ActionController::Session::TorqueBoxStore )
    class ActionController::Request
      def reset_session
        session.destroy if session
        self.session = {}
        @env['action_dispatch.request.flash_hash'] = nil
      end
    end
  end
end
  
