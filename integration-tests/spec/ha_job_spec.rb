require 'spec_helper'

require 'fileutils'
require 'torquebox-messaging'

describe "HA jobs test" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/alacarte/ha-jobs
      env: development
    
    environment:
      BASEDIR: #{File.dirname(__FILE__)}/..
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  before do
    @touchfile = Pathname.new( "./target/hajobs-touchfile.txt" )
    FileUtils.rm_rf( @touchfile )
  end

  it "should work" do
    release = TorqueBox::Messaging::Queue.new('/queue/backchannel').receive( :timeout => 120_000 )
    release.should == 'release'
    @touchfile.should exist
  end

end

