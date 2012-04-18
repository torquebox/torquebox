require 'spec_helper'


unless TorqueSpec.domain_mode   # singleton services won't even deploy [TORQUE-776]

describe "basic rack test with service injection" do

  deploy <<-END.gsub(/^ {4}/,'')

    application:
      root: #{File.dirname(__FILE__)}/../apps/rack/service-injection
      env: development
    web:
      context: /service-injection
    ruby:
      version: #{RUBY_VERSION[0,3]}

  END

  it "should work" do
    visit "/service-injection"
    page.find("#success")[:class].should == 'service-injection'
  end

end

end
