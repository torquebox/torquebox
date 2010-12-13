require 'container'
require 'open-uri'

describe "basic rack test" do

  deploy "rack/1.1.0/basic-rack.yml", :run_mode => :client

  it "should not get a 500 server error" do
    result = open("http://localhost:8080/basic-rack").read
  end

end
