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
