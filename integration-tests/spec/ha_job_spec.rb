require 'spec_helper'

require 'fileutils'
require 'torquebox-messaging'

describe "HA jobs test" do

  deploy "alacarte/hajobs-knob.yml"

  before do
    @touchfile = Pathname.new( "./target/hajobs-touchfile.txt" )
    FileUtils.rm_rf( @touchfile )
  end

  it "should work" do
    release = TorqueBox::Messaging::Queue.new('/queues/backchannel').receive( :timeout => 120_000 )
    release.should == 'release'
    @touchfile.should exist
  end

end

