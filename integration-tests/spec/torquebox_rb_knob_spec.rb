require 'spec_helper'

describe "loading torquebox.rb from a knob" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/rack/torquebox_rb_knob.knob
      env: development
    web:
      context: /torquebox_rb_knob
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should have loaded torquebox.rb" do
    visit "/torquebox_rb_knob"
    page.should have_content('HAM=BISCUIT')
  end
end
