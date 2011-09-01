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
    Thing.delete_all
  end
    
  it "should create a Thing in response to a message" do
    @input.publish("happy path")
    60.times do
      sleep 1
      break if Thing.uncached { Thing.count > 0 }
    end
    Thing.count.should == 1
    Thing.find_by_name("happy path").name.should == "happy path"
  end

  it "should create a Thing in response to a message" do
    pending("til we get a real JMS XA connection factory in place")
    @input.publish("this will error")
    20.times do
      sleep 1
      break if Thing.uncached { Thing.count > 0 }
    end
    Thing.count.should == 0
    Thing.find_all_by_name("this will error").should be_empty
  end

end
