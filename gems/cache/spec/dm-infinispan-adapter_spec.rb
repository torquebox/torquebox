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
require 'dm-core'
require 'dm-core/spec/shared/adapter_spec'
require 'datamapper/model'

describe DataMapper::Adapters::InfinispanAdapter do

  before :all do
    @adapter = DataMapper.setup(:default, :adapter => 'infinispan')
    @heffalump_index = File.join( File.dirname(__FILE__), '..', 'rubyobj.Heffalump' )
  end

  after :all do
    @adapter.stop
    FileUtils.rm_rf @heffalump_index
  end
  

  it_should_behave_like 'An Adapter'

  describe "with persistence" do
    before :each do
      @configured_dir  = File.join( File.dirname(__FILE__), '..', random_string )
      @default_dir     = File.join(File.dirname(__FILE__), '..', 'Infinispan-FileCacheStore')
      FileUtils.mkdir( @configured_dir )
    end

    it "should store data in a configured directory" do
      DataMapper.setup(:default, :adapter => 'infinispan', :persist => @configured_dir.to_s )
      heffalump = Heffalump.create
      File.exist?("#{@configured_dir.to_s}/___defaultcache").should be_true
      heffalump.should_not be_nil
    end

    it "should store data in a default directory" do
      DataMapper.setup(:default, :adapter => 'infinispan', :persist=>true)
      heffalump = Heffalump.create
      File.exist?( @default_dir ).should be_true
      heffalump.should_not be_nil
    end

    it "should store dates" do
      class Snuffleupagus
        include DataMapper::Resource
        property :id, Serial
        property :birthday, Date
      end
      Snuffleupagus.configure_index!
      adapter = DataMapper.setup(:default, :adapter => 'infinispan')
      snuffy = Snuffleupagus.create(:birthday => Date.today)
      snuffy.should_not be_nil
      snuffy.getBirthday.should_not be_nil
      adapter.stop
      FileUtils.rm_rf( File.join( File.dirname(__FILE__), '..', 'rubyobj.Snuffleupagus' ) )
    end

    after :each do
      FileUtils.rm_rf( @configured_dir )
      FileUtils.rm_rf( @default_dir )
    end
  end
end


