require 'spec_helper'
require 'set'

describe "rack reloading" do
  mutable_app 'rack/reloader'
  deploy 'rack/reloader-knob.yml'

  it "should reload" do
    visit "/reloader-rack?0"
    element = page.find_by_id("success")
    element.should_not be_nil
    element.text.should == 'INITIAL'

    seen_values = Set.new
    seen_values << element.text
    counter = 1
    while seen_values.size <= 3 && counter < 20 do
      visit "/reloader-rack?#{counter}"
      element = page.find_by_id("success")
      element.should_not be_nil
      seen_values << element.text
      counter += 1
    end

    seen_values.size.should > 3
  end
  
end
