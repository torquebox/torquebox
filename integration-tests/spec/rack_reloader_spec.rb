require 'spec_helper'

describe "rack reloading" do
  mutable_app 'rack/reloader'
  deploy :path => 'rack/reloader-knob.yml'

  if TESTING_ON_WINDOWS
    it "should reload" do
      visit "/reloader-rack"
      element = page.find_by_id("success")
      element.should_not be_nil
      element.text.should == 'INITIAL'

      seen_values = []
      seen_values << element.text
      10.times do
        sleep(3)
        visit "/reloader-rack"
        element = page.find_by_id("success")
        element.should_not be_nil
        seen_values << element.text
      end

      seen_values.size.should > 3
    end

  else

    it "should reload" do
      visit "/reloader-rack"
      element = page.find_by_id("success")
      element.should_not be_nil
      element.text.should == 'INITIAL'

      sleep(3)

      visit "/reloader-rack"
      element = page.find_by_id("success")
      element.should_not be_nil
      element.text.should == '0'

      sleep(3)

      visit "/reloader-rack"
      element = page.find_by_id("success")
      element.should_not be_nil
      element.text.should == '1'

      visit "/reloader-rack"
      element = page.find_by_id("success")
      element.should_not be_nil
      element.text.should == '1'

      visit "/reloader-rack"
      element = page.find_by_id("success")
      element.should_not be_nil
      element.text.should == '1'

      sleep(3)

      visit "/reloader-rack"
      element = page.find_by_id("success")
      element.should_not be_nil
      element.text.should == '4'

    end
  end
end
