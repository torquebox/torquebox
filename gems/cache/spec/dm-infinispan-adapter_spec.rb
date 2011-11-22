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
require 'datamapper/model'

describe DataMapper::Adapters::InfinispanAdapter do

  before :all do
    @adapter = DataMapper.setup(:default, :adapter => 'infinispan')
    @heffalump_index = File.join( File.dirname(__FILE__), '..', 'rubyobj.Heffalump' )
    class ::Heffalump
      include DataMapper::Resource

      property :id,        Serial
      property :color,     String
      property :num_spots, Integer
      property :striped,   Boolean
    end

    DataMapper.finalize
    Heffalump.auto_migrate!
  end

  it "should use the infinispan search manager" do
    @adapter.search_manager.should_not be_nil
  end

  after :all do
    @adapter.stop
    FileUtils.rm_rf @heffalump_index
  end

  describe '#auto_migrate!' do
    it 'should clear the cache' do
      Heffalump.create(:color => 'magenta')
      Heffalump.all.size.should == 1
      Heffalump.auto_migrate!
      Heffalump.all.size.should == 0
    end
  end
  
  describe '#auto_upgrade!' do
    it 'should not clear the cache' do
      Heffalump.create(:color => 'magenta')
      Heffalump.all.size.should == 1
      Heffalump.auto_upgrade!
      Heffalump.all.size.should == 1
    end
  end
  
  describe '#create' do
    it 'should not raise any errors' do
      lambda {
        Heffalump.create(:color => 'peach')
      }.should_not raise_error
    end

    it 'should set the identity field for the resource' do
      heffalump = Heffalump.new(:color => 'peach')
      heffalump.id.should be_nil
      heffalump.save
      heffalump.id.should_not be_nil
    end
  end

  describe '#read' do
    before :all do
      @heffalump = Heffalump.create(:color => 'brownish hue')
    end

    it 'should not raise any errors' do
      lambda {
        Heffalump.all()
      }.should_not raise_error
    end

    it 'should return stuff' do
      Heffalump.all.should be_include(@heffalump)
    end
  end

  describe '#update' do
    before do
      @heffalump = Heffalump.create(:color => 'indigo')
    end

    it 'should not raise any errors' do
      lambda {
        @heffalump.color = 'violet'
        @heffalump.save
      }.should_not raise_error
    end

    it 'should not alter the identity field' do
      id = @heffalump.id
      @heffalump.color = 'violet'
      @heffalump.save
      @heffalump.id.should == id
    end

    it 'should update altered fields' do
      @heffalump.color = 'violet'
      @heffalump.save
      Heffalump.get(*@heffalump.key).color.should == 'violet'
    end

    it 'should not alter other fields' do
      color = @heffalump.color
      @heffalump.num_spots = 3
      @heffalump.save
      Heffalump.get(*@heffalump.key).color.should == color
    end
  end

  describe '#delete' do
    before do
      @heffalump = Heffalump.create(:color => 'forest green')
    end

    it 'should not raise any errors' do
      lambda {
        @heffalump.destroy
      }.should_not raise_error
    end

    it 'should delete the requested resource' do
      id = @heffalump.id
      @heffalump.destroy
      Heffalump.get(id).should be_nil
    end
  end

  describe 'query matching' do
    before :all do
      Heffalump.auto_migrate!
      @red  = Heffalump.create(:color => 'red')
      @two  = Heffalump.create(:num_spots => 2)
      @five = Heffalump.create(:num_spots => 5)
    end

    describe 'conditions' do
      describe 'eql' do
        it 'should be able to search for objects included in an inclusive range of values' do
          Heffalump.all(:num_spots => 1..5).should be_include(@five)
        end

        it 'should be able to search for objects included in an exclusive range of values' do
          Heffalump.all(:num_spots => 1...6).should be_include(@five)
        end

        it 'should not be able to search for values not included in an inclusive range of values' do
          Heffalump.all(:num_spots => 1..4).should_not be_include(@five)
        end

        it 'should not be able to search for values not included in an exclusive range of values' do
          Heffalump.all(:num_spots => 1...5).should_not be_include(@five)
        end
      end

      describe 'not' do
        it 'should be able to search for objects with not equal value' do
          Heffalump.all(:color.not => 'red').should_not be_include(@red)
        end

        it 'should include objects that are not like the value' do
          Heffalump.all(:color.not => 'black').should be_include(@red)
        end

        it 'should be able to search for objects with not nil value' do
          Heffalump.all(:color.not => nil).should be_include(@red)
        end

        it 'should not include objects with a nil value' do
          Heffalump.all(:color.not => nil).should_not be_include(@two)
        end

        it 'should be able to search for object with a nil value using required properties' do
          Heffalump.all(:id.not => nil).should == [ @red, @two, @five ]
        end

        it 'should be able to search for objects not in an empty list (match all)' do
          Heffalump.all(:color.not => []).should == [ @red, @two, @five ]
        end

        it 'should be able to search for objects in an empty list and another OR condition (match none on the empty list)' do
          Heffalump.all(
            :conditions => DataMapper::Query::Conditions::Operation.new(
              :or,
              DataMapper::Query::Conditions::Comparison.new(:in, Heffalump.properties[:color], []),
              DataMapper::Query::Conditions::Comparison.new(:in, Heffalump.properties[:num_spots], [5])
            )
          ).should == [ @five ]
        end

        it 'should be able to search for objects not included in an array of values' do
          Heffalump.all(:num_spots.not => [ 1, 3, 5, 7 ]).should be_include(@two)
        end

        it 'should be able to search for objects not included in an array of values' do
          Heffalump.all(:num_spots.not => [ 1, 3, 5, 7 ]).should_not be_include(@five)
        end

        it 'should be able to search for objects not included in an inclusive range of values' do
          Heffalump.all(:num_spots.not => 1..4).should be_include(@five)
        end

        it 'should be able to search for objects not included in an exclusive range of values' do
          Heffalump.all(:num_spots.not => 1...5).should be_include(@five)
        end

        it 'should not be able to search for values not included in an inclusive range of values' do
          Heffalump.all(:num_spots.not => 1..5).should_not be_include(@five)
        end

        it 'should not be able to search for values not included in an exclusive range of values' do
          Heffalump.all(:num_spots.not => 1...6).should_not be_include(@five)
        end
      end

      describe 'like' do
        it 'should be able to search for objects that match value' do
          Heffalump.all(:color.like => '%ed').should be_include(@red)
        end

        it 'should not search for objects that do not match the value' do
          Heffalump.all(:color.like => '%blak%').should_not be_include(@red)
        end
      end

      describe 'regexp' do
        before do
          if (defined?(DataMapper::Adapters::SqliteAdapter) && @adapter.kind_of?(DataMapper::Adapters::SqliteAdapter) ||
              defined?(DataMapper::Adapters::SqlserverAdapter) && @adapter.kind_of?(DataMapper::Adapters::SqlserverAdapter))
            pending 'delegate regexp matches to same system that the InMemory and YAML adapters use'
          end
        end

        it 'should be able to search for objects that match value' do
          Heffalump.all(:color => /ed/).should be_include(@red)
        end

        it 'should not be able to search for objects that do not match the value' do
          Heffalump.all(:color => /blak/).should_not be_include(@red)
        end

        it 'should be able to do a negated search for objects that match value' do
          Heffalump.all(:color.not => /blak/).should be_include(@red)
        end

        it 'should not be able to do a negated search for objects that do not match value' do
          Heffalump.all(:color.not => /ed/).should_not be_include(@red)
        end

      end

      describe 'gt' do
        it 'should be able to search for objects with value greater than' do
          Heffalump.all(:num_spots.gt => 1).should be_include(@two)
        end

        it 'should not find objects with a value less than' do
          Heffalump.all(:num_spots.gt => 3).should_not be_include(@two)
        end
      end

      describe 'gte' do
        it 'should be able to search for objects with value greater than' do
          Heffalump.all(:num_spots.gte => 1).should be_include(@two)
        end

        it 'should be able to search for objects with values equal to' do
          Heffalump.all(:num_spots.gte => 2).should be_include(@two)
        end

        it 'should not find objects with a value less than' do
          Heffalump.all(:num_spots.gte => 3).should_not be_include(@two)
        end
      end

      describe 'lt' do
        it 'should be able to search for objects with value less than' do
          Heffalump.all(:num_spots.lt => 3).should be_include(@two)
        end

        it 'should not find objects with a value less than' do
          Heffalump.all(:num_spots.gt => 2).should_not be_include(@two)
        end
      end

      describe 'lte' do
        it 'should be able to search for objects with value less than' do
          Heffalump.all(:num_spots.lte => 3).should be_include(@two)
        end

        it 'should be able to search for objects with values equal to' do
          Heffalump.all(:num_spots.lte => 2).should be_include(@two)
        end

        it 'should not find objects with a value less than' do
          Heffalump.all(:num_spots.lte => 1).should_not be_include(@two)
        end
      end
    end

    describe 'limits' do
      it 'should be able to limit the objects' do
        Heffalump.all(:limit => 2).length.should == 2
      end
    end
  end

  describe "with persistence" do
    before :all do
      @configured_dir  = File.join( File.dirname(__FILE__), '..', random_string + "-dm-infinispan-adapter.cache" )
      @default_dir     = File.join(File.dirname(__FILE__), '..', 'Infinispan-FileCacheStore')
      @snuffy          = File.join( File.dirname(__FILE__), '..', 'rubyobj.Snuffleupagus' ) 
      FileUtils.mkdir( @configured_dir )
      class Snuffleupagus
        include DataMapper::Resource
        property :id, Serial
        property :birthday, Date
      end
      Snuffleupagus.configure_index!
    end

    after :all do
      #FileUtils.rm_rf( @configured_dir )
      #FileUtils.rm_rf( @default_dir )
      FileUtils.rm_rf( @snuffy )
    end

    it "should store data in a configured directory" do
      adapter = DataMapper.setup(:dminfinispanadapterconfigured, :adapter => 'infinispan', :persist => @configured_dir.to_s)
      snuffy = Snuffleupagus.create(:birthday=>Date.today)
      File.exist?("#{@configured_dir.to_s}/dminfinispanadapterconfigured").should be_true
      snuffy.should_not be_nil
      adapter.stop
    end

    it "should store data in a default directory" do
      adapter = DataMapper.setup(:dminfinispanadapterdefault, :adapter => 'infinispan', :persist=>true)
      snuffy = Snuffleupagus.create(:birthday=>Date.today)
      File.exist?( @default_dir ).should be_true
      snuffy.should_not be_nil
      snuffy.id.should_not be_nil
      adapter.stop
    end

    it "should store dates" do
      adapter = DataMapper.setup(:default, :adapter => 'infinispan', :persist=>true)
      snuffy = Snuffleupagus.create(:birthday => Date.today)
      snuffy.should_not be_nil
      snuffy.getBirthday.should_not be_nil
      adapter.stop
    end

  end
end


