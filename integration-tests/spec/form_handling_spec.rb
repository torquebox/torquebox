require 'spec_helper'

describe "rails3 form handling" do

  deploy "rails3/basic-knob.yml"

  it "should render properly the first time" do
    visit "/basic-rails/form_handling"
    page.driver.cookies.count.should == 1
    session_id = page.driver.cookies["JSESSIONID"].value
    session_id.length.should be > 0
    page.has_selector?("#the-form")

    auth_token = find(:xpath, "//input[@name='authenticity_token']").value
    auth_token.length.should be > 0
    find("#the-value").value.should == ""
    
    fill_in('value', :with => "the value I submit")
    click_button("Save changes")

    find("#the-value").value.should == "the value I submit is returned"
    find(:xpath, "//input[@name='authenticity_token']").value.should == auth_token
  end

  it "should support flash uploading by matrix url" do
    visit "/basic-rails/form_handling/upload_file"
    page.driver.cookies.count.should == 1
    session_id = page.driver.cookies["JSESSIONID"].value
    session_id.length.should be > 0
    auth_token = find(:xpath, "//input[@name='authenticity_token']").value
    auth_token.length.should be > 0
    find("#the-upload-form")["action"].should include(session_id)

    attach_file("the-file", File.expand_path( File.join( File.dirname( __FILE__ ), "..", "target/test-classes/org/torquebox/integration/arquillian/rails3/data.txt" ) ) )
    
    page.driver.cookies.clear
    click_button("Save changes")

    find("#data").text.should == "Just some data. As returned."
    page.driver.cookies["JSESSIONID"].should be_nil
  end

end
