require 'spec_helper'

require 'fileutils'
require 'org.torquebox.messaging-client'

describe "messaging alacarte rack test" do

  deploy :name=>'vhosting-test', :paths=> [ 
    "alacarte/messaging-knob.yml",
  ]

  it "should work" do
    touchfile = Pathname.new( "./target/messaging-touchfile.txt" )
    FileUtils.rm_rf( touchfile )

    tstamp = Time.now
    queue = TorqueBox::Messaging::Queue.new('/queues/simple_queue')
    queue.publish( { :tstamp=>tstamp, :cheese=>"gouda" } )
    sleep(2)
    touchfile.should exist
    content = File.read( touchfile ).strip
    content.should eql( "#{tstamp} // gouda" )
  end

end
