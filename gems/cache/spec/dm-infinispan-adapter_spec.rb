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

  describe 'query matching' do
    before :all do
      @red  = Heffalump.create(:color => 'red')
      @two  = Heffalump.create(:num_spots => 2)
      @five = Heffalump.create(:num_spots => 5)
      puts "CREATED: #{@red.inspect}"
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
          all = Heffalump.all(:color.not => 'red')
          all.should be_include(@two)
          all.should be_include(@five)
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
    before :each do
      @configured_dir  = File.join( File.dirname(__FILE__), '..', random_string )
      @default_dir     = File.join(File.dirname(__FILE__), '..', 'Infinispan-FileCacheStore')
      FileUtils.mkdir( @configured_dir )
    end

    it "should store data in a configured directory" do
      DataMapper.setup(:default, :adapter => 'infinispan', :persist => @configured_dir.to_s )
      heffalump = Heffalump.create
      File.exist?("#{@configured_dir.to_s}/default").should be_true
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


