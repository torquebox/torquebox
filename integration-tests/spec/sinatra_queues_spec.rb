require 'spec_helper'

describe "sinatra queues test" do

  deploy "sinatra/queues-knob.yml"

  it "should scream toby crawley" do
    visit "/uppercaser/up/toby+crawley"
    page.should have_content('TOBY CRAWLEY')
  end

  it "should be employed" do
    visit "/uppercaser/job"
    page.should have_content('employment!')
  end

end
