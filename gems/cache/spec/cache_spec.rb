#
# Copyright 2011 Red Hat, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

require File.dirname(__FILE__) + '/spec_helper'

describe TorqueBox::Infinispan::Cache do
  before :each do
    manager = org.infinispan.manager.DefaultCacheManager.new 
    service = org.projectodd.polyglot.cache.as.CacheService.new
    service.stub!(:cache_container).and_return( manager )
    TorqueBox::ServiceRegistry.stub!(:[]).with(org.projectodd.polyglot.cache.as.CacheService::CACHE).and_return( service )
    @cache = TorqueBox::Infinispan::Cache.new( :name => 'foo-cache' )
  end

  after :each do
    @cache.clear
  end

  it "should have a name" do
    @cache.name.should == 'foo-cache'
  end

  it "should respond to clustering_mode" do
    @cache.should respond_to( :clustering_mode ) 
  end

  it "should accept and return strings" do
    @cache.put('foo', 'bar').should be_true
    @cache.get('foo').should == 'bar'
  end

  it "should accept and return ruby objects" do
    heffalump = Snuffleuffagus.new(100, 'snuffle')
    @cache.put('heffalump', heffalump).should be_true
    rheffalump = @cache.get('heffalump')
    rheffalump.name.should == heffalump.name
    rheffalump.id.should == heffalump.id
  end

  it "should return all keys" do
    @cache.put('one', 1)
    @cache.put('two', 2)
    @cache.put('three', 3)
    keys = @cache.keys
    keys.length.should == 3
    keys.include?('one').should be_true
    keys.include?('two').should be_true
    keys.include?('three').should be_true
  end

  it "should allow removal of a key/value" do
    @cache.put('foo', 'bar')
    @cache.keys.length.should == 1
    @cache.remove('foo').should be_true
    @cache.keys.length.should == 0
  end

  it "should only insert on put_if_absent if the key is not already in the cache" do
    @cache.put_if_absent('foo', 'bar').should be_true
    @cache.put_if_absent('foo', 'foobar')
    @cache.get('foo').should == 'bar'
  end

  it "should clear" do
    @cache.clear.should be_true
  end

  it "should replace existing string values" do
      key = 'thekey'
      current_value = '{value:1}'
      new_value     = '{value:2}'
      @cache.put(key, current_value)
      @cache.get(key).should == current_value
      @cache.replace(key, current_value, new_value)
      @cache.get(key).should == new_value
  end
  
  it "should replace existing ruby object values" do
      key = 'thekey'
      current_value = Snuffleuffagus.new(1, 'foo')
      new_value     = Snuffleuffagus.new(2, 'bar')
      @cache.put(key, current_value)
      @cache.get(key).should == current_value
      @cache.replace(key, current_value, new_value)
      @cache.get(key).name.should == new_value.name
  end

  it "should not replace existing string values if the expected value is different" do
      key = 'string key'
      current_value = '{value:1}'
      new_value     = '{value:2}'
      @cache.put(key, current_value)
      @cache.get(key).should == current_value
      @cache.replace(key, 'something else', new_value)
      @cache.get(key).should == current_value
  end

  it "should not replace existing ruby object values if the expected value is different" do
      key = 'ruby object key'
      current_value = Snuffleuffagus.new(1, 'foo')
      new_value     = Snuffleuffagus.new(2, 'bar')
      @cache.put(key, current_value)
      @cache.get(key).should == current_value
      @cache.replace(key, new_value, new_value)
      @cache.get(key).should == current_value
  end

  it "should store java objects" do
    entry = java.util.HashMap.new
    entry.put( "Snuffleuffagus", "{color: brown}" )
    @cache.put('Snuffleuffagus/1', entry)
    @cache.get('Snuffleuffagus/1').should_not be_nil
  end

  it "should increment a sequence" do
    @cache.increment("My Sequence Name", 1).should == 1
    @cache.increment("My Sequence Name", 1).should == 2
  end

  it "should increment a sequence by a user-specified amount" do
    @cache.increment("My Sequence Name", 9).should == 9
    @cache.increment("My Sequence Name", 9).should == 18
  end

  it "should store and retrieve false values" do
    @cache.put('a false value', false)
    @cache.contains_key?('a false value').should be_true
    @cache.get('a false value').should be_false
  end

  it "should allow symbols as keys for basic put" do
    @cache.put(:asymbol, "a value")
    @cache.get(:asymbol).should == "a value"
  end

  it "should allow symbols as keys for put_if_absent" do
    @cache.put_if_absent(:asymbol, "a value")
    @cache.get(:asymbol).should == "a value"
  end

  it "should allow symbols as keys for increment" do
    @cache.increment :countsymbol
    @cache.get(:countsymbol).should == "1"
    @cache.increment(:countsymbol).should == 2
  end

  it "should store and retrieve zero" do
    @cache.put(:mynumber, 0)
    @cache.get(:mynumber).should == 0
  end

  it "should store and retrieve integers" do
    @cache.put(:mynumber, 30)
    @cache.get(:mynumber).should == 30
  end

  it "should store and retrieve floats" do
    @cache.put(:mynumber, 1.0)
    @cache.get(:mynumber).should == 1.0
  end

  it "should expire entries based on provided expiry durations" do
    cache = TorqueBox::Infinispan::Cache.new( :name => 'expiring-cache' )
    cache.put("foo", "bar", 0.1)
    sleep 1
    cache.get("foo").should be_nil
  end

  describe "with JTA transactions" do

    it "should should be transactional by default" do
      @cache.transactional?.should be_true
      @cache.transaction_mode.should == org.infinispan.transaction.TransactionMode::TRANSACTIONAL
    end

    it "should support non-transactional mode" do
      cache = TorqueBox::Infinispan::Cache.new( :name => 'non-transactional-cache', :transaction_mode => false )
      cache.transactional?.should be_false
      cache.transaction_mode.should == org.infinispan.transaction.TransactionMode::NON_TRANSACTIONAL
      begin
        cache.transaction do
          cache.put "key1", "G"
          raise "An exception"
          cache.put "key2", "C"
        end
      rescue Exception => e
        e.message.should == "An exception"
        cache.get("key1").should == "G"
        cache.get("key2").should be_nil
      end
      cache.stop
    end

    it "should use optimisitic locking mode by default" do
      @cache.locking_mode.should == org.infinispan.transaction.LockingMode::OPTIMISTIC
    end

    it "should support pessimistic locking mode" do
      cache = TorqueBox::Infinispan::Cache.new( :name => 'non-transactional-cache', :locking_mode => :pessimistic )
      cache.locking_mode.should == org.infinispan.transaction.LockingMode::PESSIMISTIC
      cache.stop
    end

    it "should accept transactional blocks" do
      @cache.transaction do |cache|
        cache.put('Frankie', 'Vallie')
      end
      @cache.get('Frankie').should == 'Vallie'
    end

    it "should behave like a transaction" do
      begin
        @cache.transaction do |cache|
          cache.put('Tommy', 'Dorsey')
          cache.put('Elvis', 'Presley')
          raise "yikes!"
        end
      rescue
      end
      @cache.get('Tommy').should be_nil
      @cache.get('Elvis').should be_nil
    end

    it "should handle multiple transactions" do
      begin
        @cache.transaction do |cache|
          cache.put('Tommy', 'Dorsey')
          raise "yikes!"
          cache.put('Elvis', 'Presley')
        end
      rescue
      end
      @cache.get('Tommy').should be_nil
      @cache.get('Elvis').should be_nil
      @cache.transaction do |cache|
        cache.put('Tommy', 'Dorsey')
        cache.put('Elvis', 'Presley')
      end
      @cache.get('Tommy').should == 'Dorsey'
      @cache.get('Elvis').should == 'Presley'
    end
  end

  describe "with persistence" do
    before(:all) do
      @default_dir    = File.join(File.dirname(__FILE__), '..', 'Infinispan-FileCacheStore')
      @configured_dir = File.join( File.dirname(__FILE__), '..', random_string + "-persisted.cache" )
      @date_cfg_dir   = File.join( File.dirname(__FILE__), '..', random_string + "-persisted-date.cache" )
      @index_dir      = File.join( File.dirname(__FILE__), '..', 'java.util.HashMap' )
      FileUtils.mkdir @configured_dir 
      FileUtils.mkdir @date_cfg_dir 
    end

    after(:all) do
      FileUtils.rm_rf @default_dir
      FileUtils.rm_rf @configured_dir
      FileUtils.rm_rf @date_cfg_dir
      FileUtils.rm_rf @index_dir if File.exist?( @index_dir )
    end

    it "should persist the data with a default directory" do
      cache = TorqueBox::Infinispan::Cache.new( :name => 'persisted-cache', :persist => true )
      entry = java.util.HashMap.new
      entry.put( "Hello", "world" )
      cache.put('foo', entry)
      File.exist?(@default_dir).should be_true
    end

    it "should persist the data with a configured directory" do
      cache = TorqueBox::Infinispan::Cache.new( :name => 'persisted-date-cache', :persist => @configured_dir.to_s )
      entry = java.util.HashMap.new
      entry.put( "Hello", "world" )
      cache.put('foo', entry)
      File.exist?("#{@configured_dir.to_s}/persisted-date-cache").should be_true
    end

    it "should persist dates with a configured directory" do
      cache = TorqueBox::Infinispan::Cache.new( :name => 'persisted-configured-date-cache', :persist => @date_cfg_dir.to_s )
      entry = java.util.Date.new
      cache.put('foo', entry).should be_true
      File.exist?("#{@date_cfg_dir.to_s}/persisted-configured-date-cache").should be_true
    end

    it "should evict keys from the heap" do
      cache = TorqueBox::Infinispan::Cache.new( :name => 'foo-cache' )
      cache.put("akey", "avalue")
      cache.evict( "akey" )
      # when cache is in-memory only, the key should return nil
      cache.get( "akey" ).should == nil
    end

    it "should only evict keys from the heap, not persistent storage" do
      cache = TorqueBox::Infinispan::Cache.new( :name => 'evict-cache', :persist=>true )
      cache.put("akey", "avalue")
      cache.evict( "akey" )
      cache.get( "akey" ).should == "avalue"
    end

    it "should expire entries based on provided expiry durations" do
      cache = TorqueBox::Infinispan::Cache.new( :name => 'expiring-cache', :persist=>true )
      cache.put("foo", "bar", 0.1)
      sleep 1
      cache.get("foo").should be_nil
    end

    it "should handle transactions" do
      cache = TorqueBox::Infinispan::Cache.new( :name => 'foo-cache', :persist=>true )
      begin
        @cache.transaction do
          cache.put('Tommy', 'Dorsey')
          cache.put('Elvis', 'Presley')
          raise "yikes!"
        end
      rescue
      end
      @cache.get('Tommy').should be_nil
      @cache.get('Elvis').should be_nil
    end

    it "should handle multiple transactions" do
      cache = TorqueBox::Infinispan::Cache.new( :name => 'foo-cache', :persist=>true )
      begin
        cache.transaction do |cache|
          cache.put('Tommy', 'Dorsey')
          raise "yikes!"
          cache.put('Elvis', 'Presley')
        end
      rescue
      end
      cache.get('Tommy').should be_nil
      cache.get('Elvis').should be_nil
      cache.transaction do 
        cache.put('Tommy', 'Dorsey')
        cache.put('Elvis', 'Presley')
      end
      cache.get('Tommy').should == 'Dorsey'
      cache.get('Elvis').should == 'Presley'
    end
  end

end

class Snuffleuffagus
  attr_accessor :id, :name
  
  def initialize(id=1, name=:default)
    @id = id
    @name = name
  end

  def ==(other)
    (@id == other.id) && (@name == other.name)
  end
end
