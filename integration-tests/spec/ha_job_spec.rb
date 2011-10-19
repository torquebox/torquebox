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

  describe "standalone" do
    it "should still work" do
      filename = TorqueBox::Messaging::Queue.new('/queue/backchannel').receive( :timeout => 120_000 )
      filename.should_not be_nil
      Pathname.new( filename ).should exist
      FileUtils.rm_rf( filename ) if filename
    end
  end

end

