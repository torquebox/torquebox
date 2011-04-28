require 'spec_helper'

describe "rackup files don't have to reside at the root" do

  deploy "rack/norootrackup-knob.yml"

  it "should be happy" do
    visit "/norootrackup"
    root = File.expand_path( File.join( File.dirname( __FILE__ ), '..', '/apps/rack/norootrackup' ) )
    prefix = root.start_with?("/") ? "vfs:" : "vfs:/"
    page.source.strip.downcase.should == "RACK_ROOT=#{prefix}#{root}".downcase
  end

end
