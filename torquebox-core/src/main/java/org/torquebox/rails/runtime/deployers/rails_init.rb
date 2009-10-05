# JBoss, Home of Professional Open Source
# Copyright 2009, Red Hat Middleware LLC, and individual contributors
# by the @authors tag. See the copyright.txt in the distribution for a
# full listing of individual contributors.
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
 
require 'org/torquebox/rails/runtime/deployers/as_logger'

require 'rubygems'
require 'vfs'

begin
  if ( TORQUEBOX_RAILS_LOAD_STYLE == :vendor )
    require "#{RAILS_ROOT}/vendor/rails/railties/lib/initializer"
  else
    require 'rubygems'
    if ( TORQUEBOX_RAILS_GEM_VERSION.nil? )
      gem 'rails'
    else
      gem 'rails', TORQUEBOX_RAILS_GEM_VERSION
    end
    require 'initializer'
  end

module Rails
  
  def self.vendor_rails?
    ( TORQUEBOX_RAILS_LOAD_STYLE == :vendor )
  end
  
  class Configuration
  	def set_root_path!
      @root_path = RAILS_ROOT
    end
    
=begin
    def framework_paths
      paths = %w(railties railties/lib activesupport/lib)
      paths << 'actionpack/lib' if frameworks.include? :action_controller or frameworks.include? :action_view

      [:active_record, :action_mailer, :active_resource, :action_web_service].each do |framework|
        paths << "#{framework.to_s.gsub('_', '')}/lib" if frameworks.include? framework
      end

      paths.map { |dir| 
        "#{framework_root_path}/#{dir}" 
      }
    end
=end
	end

	class Initializer
  	def set_load_path
      load_paths = configuration.load_paths + configuration.framework_paths
    	load_paths.reverse_each do |dir| 
        $LOAD_PATH.unshift(dir)
    	end
    	$LOAD_PATH.uniq!
  	end
  	#def initialize_logger
  	  #logger = ActiveSupport::BufferedLogger.new( JBoss::Rails::ASLogger.new( $JBOSS_RAILS_LOGGER ) )
      #silence_warnings { Object.const_set "RAILS_DEFAULT_LOGGER", logger }
  	#end
	end
  
end

Rails::Initializer.run(:install_gem_spec_stubs)
Rails::Initializer.run(:set_load_path)
Rails::Initializer.run(:add_gem_load_paths)
Rails::Initializer.run(:require_frameworks)
            
if ( Rails::VERSION::MAJOR == 2 )
  case ( Rails::VERSION::MINOR ) 
      when 2
        # do nothing special?
      when 3
        require 'org/torquebox/rails/web/v2_3/servlet_session'
        ActionController::Base.session_store     = JBoss::Session::Servlet
  end
end

require 'active_record/version' if defined?( ActiveRecord )

rescue => e
  puts e.backtrace
  raise e
end
