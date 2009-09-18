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
 
require %q(org/torquebox/rails/runtime/deployers/as_logger)

load_style=:gems

if ( load_style == :gems )
  require %q(rubygems)
  gem %q(rails)
  require %(initializer)
else
  require %q(vendor/rails/railties/lib/initializer)
end

module Rails
  
  def self.vendor_rails?
    true
  end
  
  class Configuration
  	def set_root_path!
      @root_path = RAILS_ROOT
    end
    
    def framework_paths
      paths = %w(railties railties/lib activesupport/lib)
      paths << 'actionpack/lib' if frameworks.include? :action_controller or frameworks.include? :action_view

      [:active_record, :action_mailer, :active_resource, :action_web_service].each do |framework|
        paths << "#{framework.to_s.gsub('_', '')}/lib" if frameworks.include? framework
      end

      paths.map { |dir| "#{framework_root_path}/#{dir}" }
    end
	end

	class Initializer
  	def set_load_path
      load_paths = configuration.load_paths + configuration.framework_paths
    	load_paths.reverse_each do |dir| 
        $LOAD_PATH.unshift(dir)
    	end
    	$LOAD_PATH.uniq!
  	end
  	def initialize_logger
  	  logger = ActiveSupport::BufferedLogger.new( JBoss::Rails::ASLogger.new( $JBOSS_RAILS_LOGGER ) )
      silence_warnings { Object.const_set "RAILS_DEFAULT_LOGGER", logger }
  	end
	end
  
end

Rails::Initializer.run(:install_gem_spec_stubs)
Rails::Initializer.run(:set_load_path)