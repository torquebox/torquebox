require 'spec_helper'

describe "rails vendor/jars/-based jar loading" do

  deploy :path => "rails/3.0.0/vendor-jars-jar-rails.yml"

  it "should work" do
    visit "/vendor-jars-jar"
    uuid = page.find( '#success' ).text
    uuid.should match(/^........-....-....-....-............$/)
  end

end
