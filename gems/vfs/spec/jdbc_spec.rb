require File.dirname(__FILE__) + '/spec_helper.rb'
java_import java.util.Properties

describe "JDBC Extensions for VFS" do

  describe "DriverManager" do

    before(:each) do
      @driver_manager = java.sql.DriverManager
    end

    describe "getConnection" do

      it "should call get_connection_with_properties if passed two arguments" do
        properties = Properties.new
        @driver_manager.should_receive(:get_connection_with_properties).
          with("url", properties)
        @driver_manager.getConnection("url", properties)
      end

      it "should call get_connection_with_username_password if passed three arguments" do
        @driver_manager.should_receive(:get_connection_with_username_password).
          with("url", "user", "password")
        @driver_manager.getConnection("url", "user", "password")
      end

    end

    describe "get_connection_with_properties" do

      it "should delegate to original method" do
        properties = Properties.new
        @driver_manager.should_receive(:get_connection_without_vfs).
          with("url", properties)
        @driver_manager.send(:get_connection_with_properties, "url", properties)
      end

      it "should fall back to connect directly" do
        properties = Properties.new
        driver = mock('driver')
        driver.should_receive(:connect).with("url", properties)
        @driver_manager.registerDriver(driver)
        @driver_manager.send(:get_connection_with_properties, "url", properties)
      end

    end

    describe "get_connection_with_username_password" do

      it "should delegate to original method" do
        @driver_manager.should_receive(:get_connection_without_vfs).
          with("url", "user", "password")
        @driver_manager.send(:get_connection_with_username_password,
                             "url", "user", "password")
      end

      it "should fall back to connect directly" do
        driver = mock('driver')
        driver.should_receive(:connect).with("url",an_instance_of(Properties))
        @driver_manager.registerDriver(driver)
        @driver_manager.send(:get_connection_with_username_password,
                             "url", "user", "password")
      end
    end

  end

end
