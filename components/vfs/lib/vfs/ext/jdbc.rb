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

class Java::java.sql::DriverManager
  class << self
    alias_method :get_connection_without_vfs, :getConnection
    alias_method :register_driver_without_vfs, :registerDriver

    def getConnection(url, user, pass)
      url = url.sub(':vfs:', ':')
      get_connection_without_vfs(url, user, pass)
    rescue => e
      # If we didn't register a driver, throw the exception
      raise e unless @driver
      # If we did register a driver, try to connection using it directly
      props = java.util.Properties.new
      props.setProperty("user", user)
      props.setProperty("password", pass)
      @driver.connect(url, props)
    end

    def registerDriver(driver)
      @driver = driver
    end
  end
end
