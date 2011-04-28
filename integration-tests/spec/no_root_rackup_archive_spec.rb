require 'spec_helper'

describe "archived rackup files don't have to reside at the root" do

  deploy "rack/norootrackup-archive-knob.yml"

  it "should be happy" do
    visit "/norootrackuparchive"
    root = File.expand_path( File.join( File.dirname( __FILE__ ), '..' ) )
    prefix = root.start_with?("/") ? "vfs:" : "vfs:/"
    page.source.should match /RACK_ROOT=#{prefix}#{root}.*\/norootrackup\.knob.*/
  end

end
