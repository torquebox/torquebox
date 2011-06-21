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
    @cache = TorqueBox::Infinispan::Cache.new( :name => 'foo-cache' )
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
      key = 'thekey'
      current_value = '{value:1}'
      new_value     = '{value:2}'
      @cache.put(key, current_value)
      @cache.get(key).should == current_value
      @cache.replace(key, 'something else', new_value)
      @cache.get(key).should == current_value
  end

  it "should not replace existing ruby object values if the expected value is different" do
      key = 'thekey'
      current_value = Snuffleuffagus.new(1, 'foo')
      new_value     = Snuffleuffagus.new(2, 'bar')
      @cache.put(key, current_value)
      @cache.get(key).should == current_value
      @cache.replace(key, 'something else', new_value)
      @cache.get(key).should == current_value
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
