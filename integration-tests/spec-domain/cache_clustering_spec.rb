require 'spec_helper_domain'
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

    context ':mode => :invalidation' do
      it 'should write to the cache on one server and invalidate on another with :sync => false' do
        cache_options="mode=inv&sync=false"
        visit "/cachey-cluster/root/torqueboxey?#{cache_options}"
        page.find("#mode").should have_content( "INVALIDATION_ASYNC" )
        visit "#{host1}/cachey-cluster/root/writecache?#{cache_options}"
        sleep 0.5 #async
        visit "#{host1}/cachey-cluster/root/readcache?#{cache_options}"
        page.find("#success").should have_content( "clustery" )
        visit "#{host2}/cachey-cluster/root/readcache?#{cache_options}"
        page.find("#success").should_not have_content( "clustery" )
        visit "#{host2}/cachey-cluster/root/writecache?#{cache_options}"
        sleep 0.5 #async
        visit "#{host2}/cachey-cluster/root/readcache?#{cache_options}"
        page.find("#success").should have_content( "clustery" )
        visit "#{host1}/cachey-cluster/root/readcache?#{cache_options}"
        page.find("#success").should_not have_content( "clustery" )
      end

      it 'should write to the cache on one server and invalidate on another with :sync => true' do
        cache_options="mode=inv&sync=true"
        visit "/cachey-cluster/root/torqueboxey?#{cache_options}"
        page.find("#mode").should have_content( "INVALIDATION_SYNC" )
        visit "#{host1}/cachey-cluster/root/writecache?#{cache_options}"
        visit "#{host1}/cachey-cluster/root/readcache?#{cache_options}"
        page.find("#success").should have_content( "clustery" )
        visit "#{host2}/cachey-cluster/root/readcache?#{cache_options}"
        page.find("#success").should_not have_content( "clustery" )
        visit "#{host2}/cachey-cluster/root/writecache?#{cache_options}"
        visit "#{host2}/cachey-cluster/root/readcache?#{cache_options}"
        page.find("#success").should have_content( "clustery" )
        visit "#{host1}/cachey-cluster/root/readcache?#{cache_options}"
        page.find("#success").should_not have_content( "clustery" )
      end
    end

    context ':mode => :dist' do
      it 'should write to the cache on one server and read values on another with :sync => false' do
        cache_options = "mode=dist&sync=false"
        visit "/cachey-cluster/root/torqueboxey?#{cache_options}"
        page.find("#mode").should have_content( "DIST_ASYNC" )
        visit "#{host1}/cachey-cluster/root/writecache?#{cache_options}"
        page.find("#success").should have_content( "clustery" )
        sleep 0.5 # async
        visit "#{host2}/cachey-cluster/root/readcache?#{cache_options}"
        page.find("#success").should have_content( "clustery" )
      end

      it 'should write to the cache on one server and read values on another with :sync => true' do
        cache_options = "mode=dist&sync=true"
        visit "/cachey-cluster/root/torqueboxey?#{cache_options}"
        page.find("#mode").should have_content( "DIST_SYNC" )
        visit "#{host1}/cachey-cluster/root/writecache?#{cache_options}"
        page.find("#success").should have_content( "clustery" )
        visit "#{host2}/cachey-cluster/root/readcache?#{cache_options}"
        page.find("#success").should have_content( "clustery" )
      end
    end

    context ':mode => :repl' do
      it 'should write to the cache on one server and read values on another wth :sync => false' do
        cache_options = "mode=repl&sync=false"
        visit "/cachey-cluster/root/torqueboxey?#{cache_options}"
        page.find("#mode").should have_content( "REPL_ASYNC" )
        visit "#{host1}/cachey-cluster/root/writecache?#{cache_options}"
        page.find("#success").should have_content( "clustery" )
        sleep 0.5 # async
        visit "#{host2}/cachey-cluster/root/readcache?#{cache_options}"
        page.find("#success").should have_content( "clustery" )
      end

      it 'should write to the cache on one server and read values on another wth :sync => true' do
        cache_options = "mode=repl&sync=true"
        visit "/cachey-cluster/root/torqueboxey?#{cache_options}"
        page.find("#mode").should have_content( "REPL_SYNC" )
        visit "#{host1}/cachey-cluster/root/writecache?#{cache_options}"
        page.find("#success").should have_content( "clustery" )
        visit "#{host2}/cachey-cluster/root/readcache?#{cache_options}"
        page.find("#success").should have_content( "clustery" )
      end
    end

  end

  describe TorqueBox::Infinispan::Cache do

    it "should default to distributed sync mode" do
      visit "/cachey-cluster/root/clustery" 
      page.find("#mode").should have_content( "DIST_SYNC" )
    end

    it 'should write to the cache on one server and read values on another by default' do
      visit "#{host1}/cachey-cluster/root/putcache"
      page.find("#success").should have_content( "clustery" )
      visit "#{host2}/cachey-cluster/root/getcache"
      page.find("#success").should have_content( "clustery" )
    end

    it 'should write to the cache on one server and read values on another using :replicated mode' do
      visit "#{host1}/cachey-cluster/root/putrepl"
      page.find("#success").should have_content( "clustery" )
      visit "#{host2}/cachey-cluster/root/getrepl"
      page.find("#success").should have_content( "clustery" )
    end

    it 'should read/write across the cluster in message processors' do
      visit "#{host1}/cachey-cluster/root/putprocessor"
      visit "#{host2}/cachey-cluster/root/getprocessor"
      page.find("#success").should have_content( "clustery" )
    end

    it 'should not throw an error when symbols are used as cache keys' do
      visit "#{host1}/cachey-cluster/root/putcache?symbol=true"
      page.find("#success").should have_content( "clustery" )
      visit "#{host2}/cachey-cluster/root/getcache?symbol=true"
      page.find("#success").should have_content( "clustery" )
    end

  end

  def host1
    "http://#{domain_host_for(:server1)}:#{domain_port_for(:server1, 8080)}"
  end

  def host2
    "http://#{domain_host_for(:server2)}:#{domain_port_for(:server2, 8080)}"
  end

end

