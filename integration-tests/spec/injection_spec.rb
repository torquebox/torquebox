require 'spec_helper'

describe "rails3 injection test" do

  deploy(:name => "injection-test",
         :path => "rails3/injection-knob.yml")

  it "should work for service defined in app/services" do
    visit "/injection/service"
    page.should have_content('it worked')
  end

end
