require 'spec_helper'

describe "rails POST behavior" do

  deploy :path => "rails3/basic-knob.yml"

  context "raw_post" do
    it "should work" do
      visit "/basic-rails/post/raw"
      fill_in "name", :with => "my-name"
      click_button "submit"
      find("#raw_post").should have_content("name=my-name")
      find("#name").should have_content("my-name")
    end
  end
end
