require 'spec_helper'

describe "rack vendor/jars/-based jar loading" do

  deploy :path => "rack/1.1.0/vendor-jars-jar-rack.yml"

  it "should work" do
    visit "/vendor-jars-jar"
    uuid = page.find( :xpath, '//pre' ).text
    uuid.should match(/^........-....-....-....-............$/)
  end

end
