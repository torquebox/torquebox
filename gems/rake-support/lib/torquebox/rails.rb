# Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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
  gem 'rails', ENV['RAILS_VERSION'] if ENV['RAILS_VERSION']
  require 'rails/version'
rescue LoadError
end

module TorqueBox
  # @api private
  class Rails

    def self.new_app( root )
      print_rails_not_installed_and_exit unless rails_installed?
      require_generators
      # Ensure ARGV[0] has the application path
      if ARGV.empty? || ARGV[0] != root
        ARGV.unshift( root )
      end
      ARGV << [ "-m", TorqueBox::Rails.template ]
      ARGV.flatten!
      if using_rails3?
        ::Rails::Generators::AppGenerator.start
      else
        ::Rails::Generator::Base.use_application_sources!
        ::Rails::Generator::Scripts::Generate.new.run(ARGV, :generator => 'app')
      end
    end

    def self.apply_template( root )
      print_rails_not_installed_and_exit unless rails_installed?
      require_generators
      if using_rails3?
        generator = ::Rails::Generators::AppGenerator.new( [root], {}, :destination_root => root )
        Dir.chdir(root)
        generator.apply TorqueBox::Rails.template
      else
        ::Rails::TemplateRunner.new( TorqueBox::Rails.template )
      end
    end


    def self.template
      "#{ENV['TORQUEBOX_HOME']}/share/rails/template.rb"
    end

    def self.rails_installed?
      defined? ::Rails::VERSION
    end

    def self.print_rails_not_installed_and_exit
      $stderr.puts "Rails not installed. Unable to load generators"
      exit 1
    end

    def self.using_rails3?
      ::Rails::VERSION::MAJOR == 3
    end

    def self.require_generators
      if using_rails3?
        require 'rails/generators'
        require 'rails/generators/rails/app/app_generator'
      else
        require 'rails_generator'
        require 'rails_generator/generators/applications/app/app_generator'
        require 'rails_generator/generators/applications/app/template_runner'
        require 'rails_generator/scripts/generate'
      end
    end
  end
end

