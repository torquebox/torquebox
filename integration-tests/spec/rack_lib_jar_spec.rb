require 'spec_helper'

describe "rack lib/-based jar loading" do

  deploy :path => "rack/1.1.0/lib-jar-rack.yml"

  it "should work" do
    visit "/lib-jar"
    uuid = page.find( :xpath, '//pre' ).text
    uuid.should match(/^........-....-....-....-............$/)
  end

end
