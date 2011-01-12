require 'spec_helper'

describe "rails PWD-based jar loading" do

  deploy :path => "rails/3.0.0/pwd-jar-rails.yml"

  it "should work" do
    visit "/pwd-jar"
    uuid = page.find( '#success' ).text
    uuid.should match(/^........-....-....-....-............$/)
  end

end
