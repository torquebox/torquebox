require 'spec_helper'

describe "basic war" do

  deploy File.join(File.dirname(__FILE__), "../target/test-classes/node-info.war")

  it "should work" do
    #pending "figuring out ClassCastException from Tomcat"
    visit "/node-info"
    page.should have_content( 'JBoss-Cloud node info' )
  end

end
