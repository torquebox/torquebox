require 'container'
require 'open-uri'

describe "basic rack test" do

  deploy :path => "rack/1.1.0/basic-rack.yml"

  # Or you can pass a block...
  # deploy do 
  #   create_archive_for_resource( "rack/1.1.0/basic-rack.yml" )
  # end

  it "should not get a 500 server error" do
    result = open("http://localhost:8080/basic-rack").read
  end

end
