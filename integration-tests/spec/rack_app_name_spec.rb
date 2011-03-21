require 'spec_helper'

describe "exposing app name to rack app" do

  deploy :path => "rack/app_name-knob.yml"

  it "set the constant and env var" do
    visit "/app_name"
    page.should have_content('constant:app_name|env:app_name')
  end
end
