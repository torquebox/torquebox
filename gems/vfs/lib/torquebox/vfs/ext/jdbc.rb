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

    # Monkey patch getConnection so we can sort out local filesystem url's for
    # SQLite (we need to remove the 'vfs:' from the url). This works
    # for activerecord-jdbc-adapter v1.1.2 and under. For v1.1.3 and
    # up, see web/src/main/java/org/torquebox/rails/boot.rb
    def getConnection(url, *params)

      # Remove any VFS prefix from the url (for SQLite)
      url = url.sub(':vfs:', ':')

      # Call the correct version based on the number of arguments
      case params.count
      when 0 then get_connection_with_url(url)
      when 1 then get_connection_with_properties(url, params.first)
      when 2 then get_connection_with_username_password(url, params[0], params[1])
      end
    end

    def registerDriver(driver)
      # Should this call the aliased method ??
      @driver = driver
    end

    private

    # The 1 param version of getConnection passing in a url
    def get_connection_with_url(url)
      get_connection_without_vfs(url)
    rescue => e
      raise e unless @driver
      # If we did register a driver, try to connect using it directly
      props = java.util.Properties.new
      @driver.connect(url, props)
    end

    # The 2 param version of getConnection passing in url and a hash of properties
    def get_connection_with_properties(url, properties)
      get_connection_without_vfs(url, properties)
    rescue => e
      raise e unless @driver
      # If we did register a driver, try to connect using it directly
      @driver.connect(url, properties)
    end

    # The 3 param version of getConnection passing in url, username and pasword
    def get_connection_with_username_password(url, user, pass)
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

  end
end
