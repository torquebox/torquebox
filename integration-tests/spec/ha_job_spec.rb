require 'spec_helper'

require 'fileutils'
require 'torquebox-messaging'

describe "HA jobs test" do

  deploy :path=>"alacarte/hajobs-knob.yml"

  before do
    @touchfile = Pathname.new( "./target/hajobs-touchfile.txt" )
    FileUtils.rm_rf( @touchfile )
  end

  it "should work" do
    sleep(2)
    @touchfile.should exist
  end

end

