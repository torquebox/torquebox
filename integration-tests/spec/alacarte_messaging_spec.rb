require 'spec_helper'

require 'fileutils'
require 'torquebox-messaging'

describe "messaging alacarte rack test" do

  deploy <<-END.gsub(/^ {4}/,'')

    application:
      root: #{File.dirname(__FILE__)}/../apps/alacarte/messaging
      env: development
    
    environment:
      BASEDIR: #{File.dirname(__FILE__)}/..
    
    ruby:
      version: #{RUBY_VERSION[0,3]}

  END

  it "should work" do
    touchfile = Pathname.new( "#{File.dirname(__FILE__)}/../target/messaging-touchfile.txt" )
    FileUtils.rm_rf( touchfile )

    tstamp = Time.now
    queue = TorqueBox::Messaging::Queue.new('/queue/simple_queue')
    queue.publish( { :tstamp=>tstamp, :cheese=>"gouda" } )
    backchannel = TorqueBox::Messaging::Queue.new('/queue/backchannel')
    release = backchannel.receive(:timeout => 120_000)
    release.should == 'release'
    touchfile.should exist
    content = File.read( touchfile ).strip
    content.should eql( "#{tstamp} // gouda" )
  end

end

describe "stateless messaging alacarte test" do
    deploy <<-END.gsub(/^ {4}/,'')
    application:
      root: #{File.dirname(__FILE__)}/../apps/alacarte/messaging
      env: production
    environment:
      BASEDIR: #{File.dirname(__FILE__)}/..
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should not retain state after execution" do
    queue = TorqueBox::Messaging::Queue.new('/queue/stateless_queue')
    backchannel = TorqueBox::Messaging::Queue.new('/queue/backchannel')
    5.times do
      queue.publish('something')
      backchannel.receive(:timeout => 120_000).should == 'done'
    end
  end
end
