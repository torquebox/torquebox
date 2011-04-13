require 'spec_helper'

describe "rack PWD-based jar loading" do

  deploy "rack/pwd-jar-knob.yml"

  it "should work" do
    visit "/pwd-jar"
    uuid = page.find( :xpath, '//body' ).text
    uuid.should match(/^........-....-....-....-............$/)
  end

end
