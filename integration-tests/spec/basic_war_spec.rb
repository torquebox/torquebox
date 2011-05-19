require 'spec_helper'

describe "basic war" do

  deploy "node-info.war"

  it "should work" do
    visit "/node-info"
    page.should have_content( 'JBoss-Cloud node info' )
  end

end
