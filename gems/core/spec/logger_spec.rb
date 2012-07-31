
require 'torquebox/logger'
require 'logger'

describe TorqueBox::Logger do

  it "should look nice for class objects" do
    require 'torquebox/service_registry'
    logger = TorqueBox::Logger.new( TorqueBox::ServiceRegistry )
    logger.error("JC: log for cache store")
  end

  it "should support the various boolean methods" do
    logger = TorqueBox::Logger.new
   
    logger.trace?.should be_false
    logger.debug?.should be_false
    logger.info?.should be_false
    logger.warn?.should be_true
    logger.error?.should be_true
    logger.fatal?.should be_true
  end

  it "should not barf on meaningless level setting" do
    logger = TorqueBox::Logger.new
    logger.level = Logger::WARN
    logger.level.should == Logger::WARN
  end

  it "should deal with blocks correctly" do 
    logger = TorqueBox::Logger.new
    logger.error "JC: message zero"
    logger.error { "JC: message" }
    logger.error "JC: message too"
  end

  it "should handle nil parameters" do
    logger = TorqueBox::Logger.new
    logger.info(nil)
  end

  it "should support the add method" do
    fake_logger = mock('logger')
    org.jboss.logging::Logger.stub!(:getLogger).and_return(fake_logger)
    logger = TorqueBox::Logger.new

    fake_logger.should_receive(:debug).with('debug')
    logger.add(Logger::DEBUG, 'debug', nil)

    fake_logger.should_receive(:info).with('info')
    logger.add(Logger::INFO, 'info', nil)

    fake_logger.should_receive(:warn).with('warning')
    logger.add(Logger::WARN, 'warning', nil)

    fake_logger.should_receive(:error).with('error')
    logger.add(Logger::ERROR, 'error', nil)

    fake_logger.should_receive(:warn).with('unknown')
    logger.add(Logger::UNKNOWN, 'unknown', nil)

    fake_logger.should_receive(:warn).with('block')
    logger.add(Logger::WARN, nil, nil) { 'block' }
  end
end

