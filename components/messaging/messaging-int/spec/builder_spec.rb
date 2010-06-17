
require 'java'
require 'torquebox/messaging/metadata_builder'

describe TorqueBox::Messaging::MetaData::Builder do

  before :each do
    @builder = TorqueBox::Messaging::MetaData::Builder.new
  end

  it "should produce an empty configuration if un-used" do
    processors = @builder.processors
    processors.should be_empty
  end

  it "should not set ruby_require_path if a class constant is provided" do
    module MockModule
      class MockProcessor
      end
    end

    @builder.subscribe( MockModule::MockProcessor, '/queues/foo' )

    processors = @builder.processors
    processors.size.should eql(1)

    proc_1 = processors.first
    proc_1.should_not be_nil
    proc_1.ruby_class_name.should eql( "MockModule::MockProcessor" )
    proc_1.ruby_require_path.should be_nil
  end

  it "should set ruby_require_path if a string is provided" do
    @builder.subscribe( "mock_module/mock_processor", '/queues/foo' )

    processors = @builder.processors
    processors.size.should eql(1)

    proc_1 = processors.first
    proc_1.should_not be_nil
    proc_1.ruby_class_name.should eql( "MockModule::MockProcessor" )
    proc_1.ruby_require_path.should eql( "mock_module/mock_processor" )
  end

  it "should set the destination name" do
    @builder.subscribe( "mock_processor", "/queues/foo" )

    processors = @builder.processors
    processors.size.should eql(1)

    proc_1 = processors.first
    proc_1.should_not be_nil
    proc_1.ruby_class_name.should eql( "MockProcessor" )
    proc_1.destination_name.should eql( "/queues/foo" )
  end

  it "should allow setting of a filter" do
    @builder.subscribe( "mock_processor", "/topics/bar", :filter=>'cost > 200' )

    processors = @builder.processors
    processors.size.should eql(1)

    proc_1 = processors.first
    proc_1.should_not be_nil
    proc_1.ruby_class_name.should eql( "MockProcessor" )
    proc_1.message_selector.should eql( 'cost > 200' )
  end

  it "should set the filter to nil by default" do
    @builder.subscribe( "mock_processor", "/topics/bar" )
    
    processors = @builder.processors
    processors.size.should eql(1)

    proc_1 = processors.first
    proc_1.should_not be_nil
    proc_1.ruby_class_name.should eql( "MockProcessor" )
    proc_1.message_selector.should be_nil
  end

  it "should handle processor config" do
    config = { :prop1=>"something" }
    @builder.subscribe( "mock_processor", "/topic/what", :config=>config )
    
    processors = @builder.processors
    processors.size.should eql(1)

    proc_1 = processors.first
    proc_1.should_not be_nil
    proc_1.ruby_class_name.should eql( "MockProcessor" )
    Marshal.load( proc_1.ruby_config ).should eql( config )
  end

  it "should allow implicit self evaluation from string" do
    contents = File.read( File.join( File.dirname(__FILE__), 'messaging.tq' ) ) 
    puts contents
    @builder.evaluate( contents )
    processors = @builder.processors
    processors.size.should eql(1)
    proc_1 = processors.first
    proc_1.should_not be_nil
    proc_1.ruby_class_name.should eql( "MyConsumer" )
  end

  it "should allow implicit self evaluation from file" do
    @builder.evaluate_file( File.join( File.dirname(__FILE__), 'messaging.tq' ) )
    processors = @builder.processors
    processors.size.should eql(1)
    proc_1 = processors.first
    proc_1.should_not be_nil
    proc_1.ruby_class_name.should eql( "MyConsumer" )
  end

end

