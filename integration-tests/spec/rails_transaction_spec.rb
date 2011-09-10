require 'spec_helper'

remote_describe "rails transactions testing" do
  require 'torquebox-messaging'

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/rails3/transactions
      env: development
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  before(:each) do
    @input  = TorqueBox::Messaging::Queue.new('/queue/input')
    @output  = TorqueBox::Messaging::Queue.new('/queue/output')
    puts "JC: deleting"
    Thing.delete_all
    puts "JC: deleted"
  end
    
  it "should create a Thing in response to a happy message" do
    @input.publish("happy path")
    30.times do
      sleep 1
      break if Thing.uncached { Thing.count > 0 }
    end
    Thing.count.should == 1
    Thing.find_by_name("happy path").name.should == "happy path"
  end

  it "should not create a Thing in response to an error prone message" do
    @input.publish("this will error")
    msgs = []
    loop do
      msg = @output.receive(:timeout => 20_000)
      raise "Didn't receive enough rollback messages" unless msg
      msgs << msg if msg == 'after_rollback'
      break if msgs.size == 10  # default number of HornetQ delivery attempts
    end
    Thing.count.should == 0
    Thing.find_all_by_name("this will error").should be_empty
  end

end
