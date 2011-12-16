require 'spec_helper'

describe "archived rackup files don't have to reside at the root" do

  deploy <<-END.gsub(/^ {4}/,'')
    --- 
    application: 
      RACK_ROOT: #{File.dirname(__FILE__)}/../apps/rack/norootrackup.knob
      RACK_ENV: development
    web: 
      rackup: foobar/config.ru
      context: /norootrackuparchive
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should be happy" do
    visit "/norootrackuparchive"
    root = normalize_path( File.join( File.dirname( __FILE__ ), '..' ) )
    page.source =~ %r{RACK_ROOT=(.*)}
    normalize_path($1).should match %r{#{root}.*[/\\]norootrackup\.knob.*}
  end

end
