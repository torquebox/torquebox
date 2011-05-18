require 'spec_helper'

describe "basic war" do

  deploy "node-info.war"

  it "should work" do
    visit "/node-info"
    puts page.body
  end

end
