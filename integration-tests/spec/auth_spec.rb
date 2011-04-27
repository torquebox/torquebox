require 'spec_helper'
require 'base64'

describe "authentication" do

  deploy "rack/basic-auth-knob.yml"

  it "should work for HTTP basic authentication" do
    credentials = "bmcwhirt@redhat.com:swordfish";
    encoded_credentials = Base64.encode64(credentials).strip
    add_request_header('Authorization', "Basic #{encoded_credentials}")
    visit "/basic-auth"
    element = page.find("#auth_header")
    element.should_not be_nil
    element.text.should == "Basic #{encoded_credentials}"
  end

end
