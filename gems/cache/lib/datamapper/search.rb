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

module Infinispan

  class Search

    def initialize(cache, deserializer)
      @cache          = cache
      @deserializer   = deserializer
      begin
        @search_manager = cache.search_manager
      rescue Exception => e
        cache.log( "Infinispan SearchManager not available for cache: #{cache.name}", 'ERROR' )
        cache.log( e.message, 'ERROR' )
      end
    end

    def search( query )
      if @search_manager
        cache_query = search_manager.get_query( build_query( query ), query.model.java_class )
        cache_query.list.collect { |record| deserialize(record) }
      else
        cache.all.select do |r| 
          record = deserialize(r) 
          record.class == query.model
        end
      end
    end

    def search_manager
      @search_manager
    end

    private
    def build_query( query )
      builder = search_manager.build_query_builder_for_class( query.model.java_class ).get
      query = query.conditions.nil? ? builder.all.create_query : handle_condition( builder, query.conditions.first ) 
      #puts "LUCENE QUERY: #{query.to_s}"
      query
    end

    def handle_condition( builder, condition )
      #puts "CONDITION: #{condition.inspect} <<<>>> #{condition}"
      #puts "CONDITION CLASS: #{condition.class}"
      #puts "CONDITION OPERANDS: #{condition.operands.inspect}" if condition.respond_to? :operands
      #puts "CONDITION VALUE: #{condition.value}"
      #puts "CONDITION SUBJECT: #{condition.subject.inspect}"
      if condition.class == DataMapper::Query::Conditions::OrOperation
        terms = condition.operands.each do |op|
          builder.bool.should( handle_condition( builder, op ) )
        end
        builder.all.create_query
      elsif condition.class == DataMapper::Query::Conditions::NotOperation
        handle_not_operation( builder, condition )
      elsif condition.class == DataMapper::Query::Conditions::EqualToComparison
        handle_equal_to( builder, condition ) 
      elsif condition.class == DataMapper::Query::Conditions::InclusionComparison
        handle_inclusion( builder, condition )
      elsif condition.class == DataMapper::Query::Conditions::RegexpComparison
        handle_regex( builder, condition )
      else
        builder.all.create_query
      end
    end

    def handle_regex( builder, condition )
      field = condition.subject.name
      # TODO Figure out how hibernate search/lucene deal with regexp
      value = condition.value.nil? ? "?*" : "*" + condition.value.source.gsub('/','') + "*"
      builder.keyword.wildcard.on_field(field).matching(value).create_query
    end

    def handle_inclusion( builder, condition )
      #puts "RANGE: #{condition.value.class} #{condition.value}"
      if condition.value.is_a? Range
        # TODO: Deal with Time
        if ((condition.subject.class == DataMapper::Property::DateTime) || 
           (condition.subject.class == DataMapper::Property::Date))
          rng = builder.range.on_field(condition.subject.name).from(convert_date(condition.value.begin)).to(convert_date(condition.value.end))
        else
          rng = builder.range.on_field(condition.subject.name).from(condition.value.begin).to(condition.value.end)
        end
        condition.value.exclude_end? ? rng.exclude_limit.create_query : rng.create_query 
      else # an Array
        match = condition.value.collect { |v| v }.join(' ')
        if match.empty?
          # we should find nothing
          builder.bool.must( builder.all.create_query ).not.create_query
        else
          builder.keyword.on_field( condition.subject.name ).matching( match ).create_query 
        end
      end
    end

    def convert_date(date)
      java.util.Date.new(Time.mktime(date.year, date.month, date.day, date.hour, date.min, date.sec, 0).to_i*1000) if date
    end

    def handle_equal_to( builder, condition )
      field = condition.subject.name
      value = condition.value.nil? ? "?*" : condition.value.to_s
      if !value.nil? && (value.include?( '?' ) || value.include?( '*' ))
        builder.keyword.wildcard.on_field(field).matching(value).create_query
      else
        builder.keyword.on_field(field).matching(value).create_query
      end
    end

    def handle_not_operation( builder, operation )
      condition = operation.operands.first
      if (condition.class == DataMapper::Query::Conditions::EqualToComparison && condition.value.nil?) 
        # not nil means everything
        everything = DataMapper::Query::Conditions::EqualToComparison.new( condition.subject, '*' )
        handle_condition( builder, everything )
      else
        builder.bool.must( handle_condition( builder, condition ) ).not.create_query
      end
    end

    def cache
      @cache
    end

    def deserialize(value)
      @deserializer.call(value)
    end

  end
end


