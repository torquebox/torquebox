require 'spec_helper'
require 'fileutils'
require 'torquebox/deploy_utils'

knob_path = TorqueBox::DeployUtils.create_archive( "basic-rails2.knob", 
                                                   File.join( File.dirname( __FILE__ ), "../apps/rails2/basic" ),
                                                   File.join( File.dirname( __FILE__ ), "../target/" ) )
old_path = knob_path.sub(/knob$/, 'rails')
FileUtils.cp( knob_path, old_path )

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

describe "basic backwards compatibility" do
  deploy "rails2/basic-rails.yml"
  it_should_behave_like "basic rails2 tests"
end
describe "basic knob compatibility" do
  deploy "rails2/basic-knob.yml"
  it_should_behave_like "basic rails2 tests"
end
describe "basic archive backwards compatibility" do
  deploy old_path
  it_should_behave_like "basic rails2 tests"
end
describe "basic archive knob compatibility" do
  deploy knob_path
  it_should_behave_like "basic rails2 tests"
end
