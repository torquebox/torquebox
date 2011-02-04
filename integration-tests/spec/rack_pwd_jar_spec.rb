require 'spec_helper'

describe "rack PWD-based jar loading" do

  deploy :path => "rack/1.1.0/pwd-jar-knob.yml"

  it "should work" do
    visit "/pwd-jar"
    uuid = page.find( :xpath, '//pre' ).text
    uuid.should match(/^........-....-....-....-............$/)
  end

end
