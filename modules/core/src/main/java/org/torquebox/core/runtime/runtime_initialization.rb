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

module TorqueBox

  class VersionSpec
    attr_reader :major, :minor, :revision, :tag, :str

    def initialize(str)
      @str = str
      @major, @minor, @revision, @tag = str.split('.')
      self.freeze 
    end

    def to_s
      @str
    end
  end

  def self.versions
    @versions ||= {}
  end

  def self.version
    self.versions[:torquebox]
  end

  def self.define_versions(logger=nil)
    self.versions[:torquebox] = VersionSpec.new( "${project.version}" )
    self.versions[:jbossas]   = VersionSpec.new( "${version.jbossas}" )
    self.versions[:jruby]     = VersionSpec.new( "${version.jruby}" )
  end

  def self.application_name=(application_name)
    @application_name = application_name
    ENV['TORQUEBOX_APP_NAME'] = application_name
  end

  def self.application_name()
    @application_name
  end

end

