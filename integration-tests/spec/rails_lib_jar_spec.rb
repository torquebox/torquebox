require 'spec_helper'

describe "rails lib/-based jar loading" do

  deploy :path => "rails/3.0.0/lib-jar-knob.yml"

  it "should work" do
    visit "/lib-jar"
    uuid = page.find( '#success' ).text
    uuid.should match(/^........-....-....-....-............$/)
  end

end
