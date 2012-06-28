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

  describe "basic operations" do
    it "should work" do
      visit "/cachey-cluster"
      page.find('#success').should have_content( "It works" )
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
      visit "/cachey-cluster/root/cacheytxthrows"
      page.find("#success").should have_content( "soft" )
    end

  end

  describe ":torque_box_store" do
    it "should use ActiveSupport::Cache::TorqueBoxStore" do
      visit "/cachey-cluster/root/torqueboxey" 
      page.find("#type").should have_content( "TorqueBoxStore" )
    end

    it "should default to invalidation async mode" do
      visit "/cachey-cluster/root/torqueboxey" 
      page.find("#mode").should have_content( "INVALIDATION_ASYNC" )
    end

    it 'should write to the cache on one server and read values on another when initialized with :mode=>:dist' do
      host1 = "http://#{domain_host_for(:server1)}:#{domain_port_for(:server1, 8080)}"
      host2 = "http://#{domain_host_for(:server2)}:#{domain_port_for(:server2, 8080)}"
      visit "#{host1}/cachey-cluster/root/writecache"
      page.find("#success").should have_content( "clustery" )
      visit "#{host2}/cachey-cluster/root/readcache"
      page.find("#success").should have_content( "clustery" )
    end

  end

  describe TorqueBox::Infinispan::Cache do

    it "should default to distributed sync mode" do
      visit "/cachey-cluster/root/clustery" 
      page.find("#mode").should have_content( "DIST_SYNC" )
    end

    it 'should write to the cache on one server and read values on another by default' do
      host1 = "http://#{domain_host_for(:server1)}:#{domain_port_for(:server1, 8080)}"
      host2 = "http://#{domain_host_for(:server2)}:#{domain_port_for(:server2, 8080)}"
      visit "#{host1}/cachey-cluster/root/putcache"
      page.find("#success").should have_content( "clustery" )
      visit "#{host2}/cachey-cluster/root/getcache"
      page.find("#success").should have_content( "clustery" )
    end

  end

end

