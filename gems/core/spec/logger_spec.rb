
require 'torquebox/logger'
require 'fileutils'
require 'logger'

shared_examples_for 'a torquebox logger' do
  it "should look nice for class objects" do
    require 'torquebox/service_registry'
    logger = TorqueBox::Logger.new(TorqueBox::ServiceRegistry)
    logger.error("JC: log for cache store")
  end

  it "should support the various boolean methods" do
    logger.should respond_to(:debug?)
    logger.should respond_to(:info?)
    logger.should respond_to(:warn?)
    logger.should respond_to(:error?)
    logger.should respond_to(:fatal?)
  end

  it "should not barf on meaningless level setting" do
    logger.level = Logger::WARN
    logger.level.should == Logger::WARN
  end

  it "should deal with blocks correctly" do
    logger.error "JC: message zero"
    logger.error { "JC: message" }
    logger.error "JC: message too"
  end

  it "should handle nil parameters" do
    logger.info(nil)
  end

  it "should support the rack.errors interface" do
    logger.should respond_to(:puts)
    logger.should respond_to(:write)
    logger.should respond_to(:flush)
  end

  it "should have a formatter" do
    logger.should respond_to(:formatter)
    logger.formatter.should_not be_nil
  end
end

describe TorqueBox::Logger do

  let(:logger) { TorqueBox::Logger.new }

  it_should_behave_like 'a torquebox logger'

  it "should support the trace boolean method" do
    logger.should respond_to(:trace?)
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

describe TorqueBox::FallbackLogger do
  before(:each) do
    @log_path = File.expand_path(File.join(File.dirname(__FILE__), '..',
                                           'target',
                                           'logger_spec_output.log'))
    ENV['TORQUEBOX_FALLBACK_LOGFILE'] = @log_path
  end

  after(:each) do
    FileUtils.rm_f(@log_path)
  end

  let(:logger) { TorqueBox::FallbackLogger.new }

  it_should_behave_like 'a torquebox logger'

  it "should let users override the fallback log file" do
    logger.info('testing fallback log file')
    File.read(@log_path).should include('testing fallback log file')
  end
end
