require 'spec_helper'
require 'fileutils'
require 'torquebox/deploy_utils'

shared_examples_for "basic rails2 tests" do

  it "should return a basic page" do
    visit "/basic-rails"
    element = page.find('#success')
    element.should_not be_nil
    element[:class].should == "basic-rails"
  end

  it "should send data" do
    visit "/basic-rails/senddata"
    page.source.should == "this is the content"
  end

  it "should send file" do
    visit "/basic-rails/sendfile"
    page.source.chomp.should == "this is the contents of the file"
  end

end

describe "basic knob compatibility" do
  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../apps/rails2/basic
      RAILS_ENV: development
    web:
      context: /basic-rails
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END
  it_should_behave_like "basic rails2 tests"
end
describe "basic archive knob compatibility" do
  deploy TorqueBox::DeployUtils.create_archive( "basic-rails2.knob", 
                                                File.join( File.dirname( __FILE__ ), "../apps/rails2/basic" ),
                                                TorqueSpec.knob_root )
  it_should_behave_like "basic rails2 tests"
end
