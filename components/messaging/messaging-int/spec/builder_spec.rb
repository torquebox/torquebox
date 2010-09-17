
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

  it "should allow implicit self evaluation from string" do
    contents = File.read( File.join( File.dirname(__FILE__), 'messaging.yml' ) ) 
    @builder.evaluate( contents )
    processors = @builder.processors
    processors.size.should eql(1)
    proc_1 = processors.first
    proc_1.should_not be_nil
    proc_1.ruby_class_name.should eql( "MyConsumer" )
  end

  it "should allow implicit self evaluation from file" do
    @builder.evaluate_file( File.join( File.dirname(__FILE__), 'messaging.yml' ) )
    processors = @builder.processors
    processors.size.should eql(1)
    proc_1 = processors.first
    proc_1.should_not be_nil
    proc_1.ruby_class_name.should eql( "MyConsumer" )
  end

end

