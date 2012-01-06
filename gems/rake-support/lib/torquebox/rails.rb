# Copyright 2008-2012 Red Hat, Inc, and individual contributors.
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

begin
  require 'rails/generators'
  require 'rails/generators/rails/app/app_generator'
rescue LoadError
  # Rails isn't installed, bail out
  return
end

module TorqueBox
  class Rails
    def self.new_app
      # Assumes ARGV[0] already has the application name
      ARGV << [ "-m", TorqueBox::Rails.template ]
      ARGV.flatten!
      ::Rails::Generators::AppGenerator.start
    end

    def self.apply_template( root )
      generator = ::Rails::Generators::AppGenerator.new( [root], {}, :destination_root => root )
      generator.apply TorqueBox::Rails.template 
    end

    def self.template
      "#{ENV['TORQUEBOX_HOME']}/share/rails/template.rb"
    end
  end
end
