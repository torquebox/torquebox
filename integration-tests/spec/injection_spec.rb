require 'spec_helper'

describe "rails3 injection test" do

  deploy(:name => "injection-test",
         :path => "rails3/injection-knob.yml")

  it "should work for services defined in app/services" do
    visit "/injection/service"
    page.should have_content('it worked')
  end

  it "should work for jobs defined in app/jobs" do
    visit "/injection/job"
    page.should have_content('it worked')
  end

  it "should work for tasks defined in app/tasks" do
    visit "/injection/task"
    page.should have_content('it worked')
  end

end
