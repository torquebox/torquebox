require 'spec_helper'

shared_examples_for "all overrides" do

  it "should return correct static content" do
    visit "#{@context}/index.html"
    element = page.find('#success')
    element[:class].should == @app
  end

  it "should return correct RACK_ENV" do
    visit "#{@context}/RACK_ENV"
    page.find("body").text.should == @env
  end

  it "should return correct RACK_ROOT" do
    visit "#{@context}/RACK_ROOT"
    page.find("body").text.should match @home
  end

end

shared_examples_for "internal overrides" do
  it_should_behave_like "all overrides"
  before(:each) do
    @context = "/override-internal"
    @app = "internal"
    @env = "production"
  end
  it "should return correct environment variables" do
    visit "#{@context}/APP"
    page.find("body").text.should == @app
    visit "#{@context}/foo"
    page.find("body").text.should == "#{@app} foo"
    visit "#{@context}/bar"
    page.find("body").text.should == "#{@app} bar"
  end
end

shared_examples_for "external overrides" do
  it_should_behave_like "all overrides"
  before(:each) do
    @context = "/override-external"
    @app = "external"
    @env = "development"
  end
  it "should return correct environment variables" do
    visit "#{@context}/APP"
    page.find("body").text.should == @app
    visit "#{@context}/foo"
    page.find("body").text.should == "internal foo"
    visit "#{@context}/bar"
    page.find("body").text.should == "maid"
    visit "#{@context}/foot"
    page.find("body").text.should == "stink"
  end
end

describe "exploded internal" do
  before(:each) do
    @home = /^vfs:.*\/override$/
  end
  deploy "sinatra/exploded-internal-knob.yml"
  it_should_behave_like "internal overrides"
end
describe "archived internal" do
  before(:each) do
    @home = /^vfs:.*\/override.knob.*\/contents$/
  end
  deploy "sinatra/archived-internal-knob.yml"
  it_should_behave_like "internal overrides"
end

describe "exploded external" do
  before(:each) do
    @home = /^vfs:.*\/override$/
  end
  deploy "sinatra/exploded-external-knob.yml"
  it_should_behave_like "external overrides"
end
describe "archived external" do
  before(:each) do
    @home = /^vfs:.*\/override.knob.*\/contents$/
  end
  deploy "sinatra/archived-external-knob.yml"
  it_should_behave_like "external overrides"
end
