require 'spec_helper'

describe "rack lib/-based jar loading" do

  deploy :path => "rack/lib-jar-knob.yml"

  it "should work" do
    visit "/lib-jar"
    uuid = page.find( :xpath, '//body' ).text
    uuid.should match(/^........-....-....-....-............$/)
  end

end
