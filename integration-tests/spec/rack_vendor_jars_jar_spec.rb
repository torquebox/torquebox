require 'spec_helper'

describe "rack vendor/jars/-based jar loading" do

  deploy :path => "rack/vendor-jars-jar-knob.yml"

  it "should work" do
    visit "/vendor-jars-jar"
    uuid = page.find( :xpath, '//body' ).text
    uuid.should match(/^........-....-....-....-............$/)
  end

end
