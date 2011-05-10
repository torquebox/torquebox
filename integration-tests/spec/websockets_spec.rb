require 'spec_helper'

describe "websockets rack test" do

  deploy "rails3/websockets-knob.yml"

  it "should load the index page" do
    visit "/websocket_demo"
    page.should have_content('Chat Example Using Stomp Over Web Sockets')
  end

end
