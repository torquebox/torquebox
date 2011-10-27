require 'spec_helper'

shared_examples_for "basic rails3 test" do

  it "should do a basic get" do
    visit "/basic-rails"
    page.should have_content('It works')
    page.find("#success")[:class].should == 'basic-rails'
  end

  it "should do a raw post" do
    visit "/basic-rails/post/raw"
    fill_in "name", :with => "my-name"
    click_button "submit"
    find("#raw_post").should have_content("name=my-name")
    find("#name").should have_content("my-name")
  end

  it "should support injection" do
    visit "/basic-rails/root/injectiontest"
    find('#success').text.should == 'taco'
  end

  it "should default to development environment" do
    visit "/basic-rails/root/environment"
    find('#success').text.should == 'development'
  end

  it "should support environment variables in database.yml" do
    visit "/basic-rails/root/databaseyml"
    find('#success').text.should == 'foobar'
  end
end

describe "basic tests with environment variables in database.yml" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../apps/rails3/basic
    environment:
      DB_USER: foobar
    web:
      context: /basic-rails
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it_should_behave_like "basic rails3 test"

end

describe "archive knobs with environment variables in database.yml" do
  deploy { TorqueBox::DeployUtils.create_archive( "basic-rails3.knob", 
                                                  File.join( File.dirname( __FILE__ ), "../apps/rails3/basic" ),
                                                  TorqueSpec.knob_root ) }
  pending "A fix" do
    it_should_behave_like "basic rails3 test"
  end
end
