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

require 'spec_helper'

describe TorqueBox::Caching do
  before :each do
    @cache = TorqueBox::Caching.cache('foo-cache')
  end

  after :each do
    @cache.clear
  end

  it "should have a name" do
    @cache.name.should == 'foo-cache'
  end

  it "should have access to the real cache" do
    @cache.cache.should be_a(java.util::Map)
    @cache.cache.should be_a(org.infinispan::Cache)
  end

  it "should have a size" do
    @cache.size.should == 0
    @cache.put('foo', 'bar')
    @cache.size.should == 1
  end

  it "should accept and return strings" do
    @cache.put('foo', 'bar').should be_nil
    @cache.get('foo').should == 'bar'
  end

  it "should work with index operators" do
    @cache[:a] = 1
    @cache[:a].should == 1
    @cache[:b] = "two"
    @cache[:b].should == "two"
  end

  it "should accept and return ruby objects" do
    heffalump = Snuffleuffagus.new(100, 'snuffle')
    @cache.put('heffalump', heffalump).should be_nil
    rheffalump = @cache.get('heffalump')
    rheffalump.name.should == heffalump.name
    rheffalump.id.should == heffalump.id
  end

  it "should accept a map" do
    @cache.put_all(:a => 1, :b => 2)
    @cache[:a].should == 1
    @cache[:b].should == 2
    x = { :aa => 11, :bb => 22 }
    @cache.put_all(x)
    @cache[:aa].should == 11
    @cache[:bb].should == 22
  end

  it "should return all keys" do
    @cache.put('one', 1)
    @cache.put('two', 2)
    @cache.put('three', 3)
    keys = @cache.keys
    keys.length.should == 3
    keys.include?('one').should be true
    keys.include?('two').should be true
    keys.include?('three').should be true
  end

  it "should return all values" do
    @cache.put(:a, 1)
    @cache.put(:b, 2)
    @cache.put(:c, 3)
    @cache.values.sort.should eql([1, 2, 3])
    @cache.entry_set.size.should == 3
  end

  it "should allow removal of a key" do
    @cache.put('foo', 'bar')
    @cache.keys.length.should == 1
    @cache.remove('foo').should == 'bar'
    @cache.keys.length.should == 0
  end

  it "should allow removal of a key mapped to specific value" do
    @cache.put('foo', 'bar')
    @cache.remove('foo', 'baz').should be false
    @cache.size.should == 1
    @cache.remove('foo', 'bar').should be true
    @cache.size.should == 0
  end

  it "should only insert on put_if_absent if the key is not already in the cache" do
    @cache.put_if_absent('foo', 'bar').should be_nil
    @cache.put_if_absent('foo', 'foobar').should == 'bar'
    @cache.get('foo').should == 'bar'
  end

  it "should clear" do
    @cache.should be_empty
    @cache['foo'] = 'bar'
    @cache.should_not be_empty
    @cache.clear.should be_empty
    @cache.should be_empty
    @cache.clear[:a] = 1
    @cache.should_not be_empty
    @cache.clear.should == @cache
  end

  it "should replace any value" do
    @cache.replace(:a, 1).should be_nil
    @cache.should be_empty
    @cache.put(:a, 1)
    @cache[:a].should == 1
    @cache.replace(:a, 2)
    @cache[:a].should == 2
  end

  it "should replace existing string values" do
    key = 'thekey'
    current_value = '{value:1}'
    new_value     = '{value:2}'
    @cache.put(key, current_value)
    @cache.get(key).should == current_value
    @cache.compare_and_set(key, current_value, new_value).should be true
    @cache.get(key).should == new_value
  end

  it "should replace existing ruby object values" do
    key = 'thekey'
    current_value = Snuffleuffagus.new(1, 'foo')
    new_value     = Snuffleuffagus.new(2, 'bar')
    @cache.put(key, current_value)
    @cache.get(key).should == current_value
    @cache.compare_and_set(key, current_value, new_value).should be true
    @cache.get(key).name.should == new_value.name
  end

  it "should not replace existing string values if the expected value is different" do
    key = 'string key'
    current_value = '{value:1}'
    new_value     = '{value:2}'
    @cache.put(key, current_value)
    @cache.get(key).should == current_value
    @cache.compare_and_set(key, 'something else', new_value).should be false
    @cache.get(key).should == current_value
  end

  it "should not replace existing ruby object values if the expected value is different" do
    key = 'ruby object key'
    current_value = Snuffleuffagus.new(1, 'foo')
    new_value     = Snuffleuffagus.new(2, 'bar')
    @cache.put(key, current_value)
    @cache.get(key).should == current_value
    @cache.compare_and_set(key, new_value, new_value).should be false
    @cache.get(key).should == current_value
  end

  it "should store java objects" do
    entry = java.util.HashMap.new
    entry.put("Snuffleuffagus", "{color: brown}")
    @cache.put('Snuffleuffagus/1', entry)
    @cache.get('Snuffleuffagus/1').should_not be_nil
  end

  it "should increment a sequence" do
    name = "My Sequence Name"
    @cache.put(name, 0)
    @cache.compare_and_set(name, 0, 1).should be true
    @cache.get(name).should == 1
    @cache.compare_and_set(name, 1, 2).should be true
    @cache.get(name).should == 2
    @cache.compare_and_set(name, 42, 3).should be false
  end

  it "should store and retrieve false values" do
    @cache.put('a false value', false)
    @cache.contains_key?('a false value').should be true
    @cache.get('a false value').should be false
  end

  it "should allow symbols as keys for basic put" do
    @cache.put(:asymbol, "a value")
    @cache.get(:asymbol).should == "a value"
  end

  it "should allow symbols as keys for put_if_absent" do
    @cache.put_if_absent(:asymbol, "a value")
    @cache.get(:asymbol).should == "a value"
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

  it "should expire entries based on constructor ttl" do
    cache = TorqueBox::Caching.cache('expiring-cache', :ttl => 100)
    cache.put("foo", "bar")
    sleep 1
    cache.get("foo").should be_nil
  end

  it "should expire entries based on method ttl" do
    @cache.put("foo", "bar", :ttl => 100)
    @cache["foo"].should == "bar"
    sleep 1
    @cache["foo"].should be_nil
  end

  describe "with JTA transactions" do

    it "should be non-transactional by default" do
      @cache.configuration.transaction.transaction_mode.should ==
        org.infinispan.transaction.TransactionMode::NON_TRANSACTIONAL
      # begin
      #   cache.transaction do
      #     cache.put "key1", "G"
      #     raise "An exception"
      #     cache.put "key2", "C"
      #   end
      # rescue Exception => e
      #   e.message.should == "An exception"
      #   cache.get("key1").should == "G"
      #   cache.get("key2").should be_nil
      # end
    end

    it "should support transactional mode" do
      cache = TorqueBox::Caching.cache('transactional-cache', :transactional => true)
      cache.configuration.transaction.transaction_mode.should ==
        org.infinispan.transaction.TransactionMode::TRANSACTIONAL
      TorqueBox::Caching.stop('transactional-cache')
    end

    it "should use optimisitic locking mode by default" do
      @cache.configuration.transaction.locking_mode.should ==
        org.infinispan.transaction.LockingMode::OPTIMISTIC
    end

    it "should support pessimistic locking mode" do
      cache = TorqueBox::Caching.cache('transactional-cache',
                                       :transactional => true,
                                       :locking => :pessimistic)
      cache.configuration.transaction.locking_mode.should == org.infinispan.transaction.LockingMode::PESSIMISTIC
      TorqueBox::Caching.stop('transactional-cache')
    end

    # it "should accept transactional blocks" do
    #   @cache.transaction do |cache|
    #     cache.put('Frankie', 'Vallie')
    #   end
    #   @cache.get('Frankie').should == 'Vallie'
    # end

    # it "should behave like a transaction" do
    #   begin
    #     @cache.transaction do |cache|
    #       cache.put('Tommy', 'Dorsey')
    #       cache.put('Elvis', 'Presley')
    #       raise "yikes!"
    #     end
    #   rescue
    #   end
    #   @cache.get('Tommy').should be_nil
    #   @cache.get('Elvis').should be_nil
    # end

    # it "should handle multiple transactions" do
    #   begin
    #     @cache.transaction do |cache|
    #       cache.put('Tommy', 'Dorsey')
    #       raise "yikes!"
    #       cache.put('Elvis', 'Presley')
    #     end
    #   rescue
    #   end
    #   @cache.get('Tommy').should be_nil
    #   @cache.get('Elvis').should be_nil
    #   @cache.transaction do |cache|
    #     cache.put('Tommy', 'Dorsey')
    #     cache.put('Elvis', 'Presley')
    #   end
    #   @cache.get('Tommy').should == 'Dorsey'
    #   @cache.get('Elvis').should == 'Presley'
    # end
  end

  describe "with persistence" do

    def cache_on_disk(dir = nil, name = nil)
      File.join(File.dirname(__FILE__), '..',
                (dir || 'Infinispan-SingleFileStore'),
                (name ? name + ".dat" : ""))
    end

    before(:all) do
      @configured_dir = random_string + "-persisted.cache"
      @date_cfg_dir   = random_string + "-persisted-date.cache"
      FileUtils.mkdir cache_on_disk(@configured_dir)
      FileUtils.mkdir cache_on_disk(@date_cfg_dir)
    end

    after(:all) do
      FileUtils.rm_rf cache_on_disk
      FileUtils.rm_rf cache_on_disk(@configured_dir)
      FileUtils.rm_rf cache_on_disk(@date_cfg_dir)
    end

    it "should persist the data with a default directory" do
      cache = TorqueBox::Caching.cache('persisted-cache', :persist => true)
      entry = java.util.HashMap.new
      entry.put("Hello", "world")
      cache.put('foo', entry)
      File.exist?(cache_on_disk(nil, 'persisted-cache')).should be true
    end

    it "should persist the data with a configured directory" do
      cache = TorqueBox::Caching.cache('persisted-date-cache',
                                       :persist => cache_on_disk(@configured_dir).to_s)
      entry = java.util.HashMap.new
      entry.put("Hello", "world")
      cache.put('foo', entry)
      File.exist?(cache_on_disk(@configured_dir, "persisted-date-cache")).should be true
    end

    it "should persist dates with a configured directory" do
      cache = TorqueBox::Caching.cache('persisted-configured-date-cache',
                                       :persist => cache_on_disk(@date_cfg_dir).to_s)
      entry = java.util.Date.new
      cache.put('foo', entry).should be_nil
      File.exist?(cache_on_disk(@date_cfg_dir, "persisted-configured-date-cache")).should be true
    end

    it "should evict keys from the heap" do
      cache = TorqueBox::Caching.cache('foo-cache')
      cache.put("akey", "avalue")
      cache.evict("akey")
      # when cache is in-memory only, the key should return nil
      cache.get("akey").should be_nil
    end

    it "should only evict keys from the heap, not persistent storage" do
      cache = TorqueBox::Caching.cache('evict-cache', :persist => true)
      cache.put("akey", "avalue")
      cache.evict("akey")
      cache.get("akey").should == "avalue"
    end

    it "should automatically evict entries over max" do
      cache = TorqueBox::Caching.cache("max-entries", :max_entries => 3)
      cache.put_all(:a => 1, :b => 2, :c => 3)
      cache[:a].should == 1
      cache[:b].should == 2
      cache[:c].should == 3
      cache.put(:d, 4)
      cache[:a].should be_nil
    end

    it "should expire entries based on provided expiry durations" do
      cache = TorqueBox::Caching.cache('expiring-cache', :persist => true, :ttl => 100)
      cache.put("foo", "bar")
      sleep 1
      cache.get("foo").should be_nil
    end

    it "should handle transactions" do
      cache = TorqueBox::Caching.cache('foo-cache', :persist => true)
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

    # it "should handle multiple transactions" do
    #   cache = TorqueBox::Caching.cache('foo-cache', :persist=>true)
    #   begin
    #     cache.transaction do |cache|
    #       cache.put('Tommy', 'Dorsey')
    #       raise "yikes!"
    #       cache.put('Elvis', 'Presley')
    #     end
    #   rescue
    #   end
    #   cache.get('Tommy').should be_nil
    #   cache.get('Elvis').should be_nil
    #   cache.transaction do
    #     cache.put('Tommy', 'Dorsey')
    #     cache.put('Elvis', 'Presley')
    #   end
    #   cache.get('Tommy').should == 'Dorsey'
    #   cache.get('Elvis').should == 'Presley'
    # end
  end

  describe "event notifications" do
    it "should add and remove cache listeners" do
      num_orig_listeners = @cache.get_listeners.size
      l1 = @cache.add_listener(:cache_entry_loaded)
      l2 = @cache.add_listener(:cache_entry_visited, :cache_entry_removed)
      l1.size.should == 1
      l2.size.should == 2
      (@cache.get_listeners.size - num_orig_listeners).should == 3
      @cache.remove_listener(l1.first)
      (@cache.get_listeners.size - num_orig_listeners).should == 2
      @cache.get_listeners.each { |x| @cache.remove_listener(x) }
      @cache.get_listeners.size.should == 0
    end

    it "should reject bad event types" do
      num_orig_listeners = @cache.get_listeners.size
      expect do
        @cache.add_listener(:cache_entry_visited, :this_should_barf)
      end.to raise_error(Java::JavaLang::IllegalArgumentException)
      (@cache.get_listeners.size - num_orig_listeners).should == 0
    end
  end
end

class Snuffleuffagus
  attr_accessor :id, :name

  def initialize(id = 1, name = :default)
    @id = id
    @name = name
  end

  def ==(other)
    (@id == other.id) && (@name == other.name)
  end
end
