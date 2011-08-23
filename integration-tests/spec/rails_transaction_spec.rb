require 'spec_helper'

remote_describe "rails transactions testing" do
  require 'torquebox-messaging'

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/rails3/transactions
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  before(:each) do
    @input  = TorqueBox::Messaging::Queue.new('/queue/input')
  end
    
  it "should create a Thing in response to a message" do
    puts "JC: connection.java_class.name=#{Thing.connection.raw_connection.connection.java_class.name}"
    puts "JC: connection.respond_to?(:getXAResource)=#{Thing.connection.raw_connection.connection.respond_to?(:getXAResource)}"
    puts "JC: underlying_connection.java_class.name=#{Thing.connection.raw_connection.connection.underlying_connection.java_class.name}"
    puts "JC: underlying_connection.respond_to?(:getXAResource)=#{Thing.connection.raw_connection.connection.underlying_connection.respond_to?(:getXAResource)}"

    pending("until we can monkey-patch arjdbc to not start transactions if our connection is XA")
    @input.publish("anything")
    sleep 5
    Thing.count.should == 1
  end
end
