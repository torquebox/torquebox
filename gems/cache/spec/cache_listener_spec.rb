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
require 'cache_listener'

describe TorqueBox::Infinispan::CacheListener do
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

  it "the cache should accept listeners" do
    @cache.should respond_to :add_listener
  end

  it "should notify when an entry is added to the cache" do
    listener = TestListener.new
    @cache.add_listener( listener )
    listener.should_receive( :cache_entry_created ).at_least :once
    @cache.put("akey", "avalue")
  end

  it "should notify when an entry is deleted from the cache" do
    listener = TestListener.new
    @cache.add_listener( listener )
    @cache.put("akey", "avalue")
    listener.should_receive( :cache_entry_removed ).at_least :once
    @cache.remove("akey")
  end

  it "should notify when an entry is retrieved from the cache" do
    listener = TestListener.new
    @cache.add_listener( listener )
    @cache.put("akey", "avalue")
    listener.should_receive( :cache_entry_visited ).at_least :once
    @cache.get("akey")
  end

  it "should notify when an entry is modified in the cache" do
    listener = TestListener.new
    @cache.add_listener( listener )
    @cache.put("akey", "avalue")
    listener.should_receive( :cache_entry_modified ).at_least :once
    @cache.put("akey", "another value")
  end

  it "should notify when an entry is activated in the cache" do
    pending "Figuring out why this doesn't work"
    listener = TestListener.new
    cache = TorqueBox::Infinispan::Cache.new( :name => 'foo-cache', :persist=>true )
    cache.add_listener( listener )
    cache.put("akey", "avalue")
    cache.evict("akey")
    listener.should_receive( :cache_entry_activated ).at_least :once
    cache.get("akey")
  end

  it "should notify when an entry is evicted in the cache" do
    listener = TestListener.new
    @cache.add_listener( listener )
    @cache.put("akey", "avalue")
    listener.should_receive( :cache_entry_evicted ).at_least :once
    @cache.evict( "akey" )
  end

end

class TestListener < TorqueBox::Infinispan::CacheListener
  def cache_entry_created(entry)   ; end
  def cache_entry_removed(entry)   ; end
  def cache_entry_visited(entry)   ; end
  def cache_entry_modified(entry)  ; end
  def cache_entry_evicted(entry)   ; end
  def cache_entry_activated(entry) ; end
end

