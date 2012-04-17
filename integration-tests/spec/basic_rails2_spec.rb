require 'spec_helper'
require 'fileutils'

shared_examples_for "basic rails2 tests" do |web_context|

  it "should return a basic page" do
    visit web_context
    element = page.find('#success')
    element.should_not be_nil
    element[:class].should == "basic-rails"
  end

  it "should send data", :browser_not_supported=>true do
    visit "#{web_context}/senddata"
    page.source.should == "this is the content"
  end
  
  it "should send file", :browser_not_supported=>true do
    visit "#{web_context}/sendfile"
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
  
  it_should_behave_like "basic rails2 tests", "/basic-rails"

  it "should be able to autoload from TB path conventions" do
    visit "/basic-rails/autoload"
    page.find('#success').should_not be_nil
  end
end

describe "basic archive knob compatibility" do
  deploy { TorqueBox::DeployUtils.create_archive( :name => "basic-rails2.knob", 
                                                  :app_dir => File.join( File.dirname( __FILE__ ), "../apps/rails2/basic" ),
                                                  :dest_dir => TorqueSpec.knob_root ) }
  it_should_behave_like "basic rails2 tests", "/"
end

describe "no web: section in torquebox.yml" do
    deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../apps/rails2/basic
      RAILS_ENV: development
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it_should_behave_like "basic rails2 tests", "/"
end
