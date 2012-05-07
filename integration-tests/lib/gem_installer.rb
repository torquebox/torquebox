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

require 'rubygems'
require 'rubygems/dependency_installer'

class GemInstaller

  def self.with(versions,&block)
    installer = GemInstaller.new( versions )
    block.call(installer)
  end

  def initialize(versions={})
    @versions = versions
    @installer = Gem::DependencyInstaller.new
    @no_deps_installer = Gem::DependencyInstaller.new(:ignore_dependencies => true)
  end

  def install(gem_name, version=nil, include_deps=true)
    if ( version.nil? )
      version = @versions[ gem_name.gsub(/-/, '_').to_sym ]
    end
    gem_dir = File.join( ENV['GEM_HOME'], 'gems', "#{gem_name}-#{version}*" )
    unless ( Dir[ gem_dir ].empty? )
      puts "Skipping #{gem_name}"
      return
    end
    puts "Must specify version of #{gem_name}" and return unless version
    puts "Installing #{gem_name} #{version}"
    installer = include_deps ? @installer : @no_deps_installer
    installer.install( gem_name, version )

  end

end
