require 'spec_helper'

remote_describe "transactions testing" do
  require 'torquebox-core'
  include TorqueBox::Injectors

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/alacarte/transactions
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should not hang when receive times out" do
    input = inject('/queue/input')
    output = inject('/queue/output')
    response = nil
    thread = Thread.new {
      response = output.receive(:timeout => 1)
      puts "JC: timed out"
    }
    puts "JC: started thread"
    input.publish("anything")
    puts "JC: published"
    thread.join
    puts "JC: joined"
    response.should be_nil
    puts "JC: done"
  end
end
