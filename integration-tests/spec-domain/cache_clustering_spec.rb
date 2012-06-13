require 'spec_helper'
require 'torquebox-cache'


describe 'cache clustering' do

  deploy <<-END.gsub(/^ {4}/, '')
    web:
      context: /cachey-cluster
    application:
      root: #{File.dirname(__FILE__)}/../apps/rails3/basic-with-cache
      env: development
  END

  it "should work" do
    visit "/cachey-cluster"
    page.find('#success').should have_content( "It works" )
  end

  it "should use ActiveSupport::Cache::TorqueBoxStore" do
    visit "/cachey-cluster/root/torqueboxey" 
    page.find("#success").should have_content( "TorqueBoxStore" )
  end

  it "should perform caching" do
    visit "/cachey-cluster/root/cachey"
    page.find('#success').should have_content( "crunchy" )
  end

  it "should transactionally cache objects in the store" do
    visit "/cachey-cluster/root/cacheytx"
    page.find("#success").should have_content( "crunchy" )
  end

  it "should rollback failed transactional cache objects in the store" do
    pending "A fix for [TORQUE-855]"
    visit "/cachey-cluster/root/cacheytxthrows"
    page.find("#success").should have_content( "soft" )
  end

  it 'should write to the cache on one server and read it on another' do
    host1 = "http://#{domain_host_for(:server1)}:#{domain_port_for(:server1, 8080)}"
    host2 = "http://#{domain_host_for(:server2)}:#{domain_port_for(:server2, 8080)}"
    visit "#{host1}/cachey-cluster/root/writecache"
    page.find("#success").should have_content( "clustery" )
    visit "#{host2}/cachey-cluster/root/readcache"
    page.find("#success").should have_content( "clustery" )
  end

end

