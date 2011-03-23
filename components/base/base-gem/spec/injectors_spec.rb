
require 'torquebox/injectors'

describe TorqueBox::Injectors do

  describe "jboss logger injection" do
    
    include TorqueBox::Injectors

    it "should return a logger identified by a class" do
      require 'active_support/cache/torque_box_store'
      logger = jboss_logger( ActiveSupport::Cache::TorqueBoxStore )
      logger.info("this should work")
      logger.name.should == "ActiveSupport::Cache::TorqueBoxStore"
    end

    it "should return a logger identified by a string" do
      logger = jboss_logger( "flibbity-jibbit" )
      logger.info("this should work")
      logger.name.should == "flibbity-jibbit"
    end

    it "should return a logger for the class if not passed an identifier" do
      class Foo
        include TorqueBox::Injectors
        def logger
          jboss_logger
        end
      end
      logger = Foo.new.logger
      logger.info("this should work")
      logger.name.should == "Foo"
    end

  end

end

