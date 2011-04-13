require 'spec_helper'

describe "rails vendor/jars/-based jar loading" do

  deploy "rails3/vendor-jars-jar-knob.yml"

  it "should work" do
    visit "/vendor-jars-jar"
    uuid = page.find( '#success' ).text
    uuid.should match(/^........-....-....-....-............$/)
  end

end
