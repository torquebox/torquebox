# Rails 2 and RubyGems shipped with >= JRuby 1.7.5 don't play together
versions = JRUBY_VERSION.split('.')[0,3]
if versions.length == 3 && versions[0].to_i >= 1 && versions[1].to_i >= 7 &&
    versions[2].to_i >= 5
  require 'rubygems'
  module Gem
    def self.source_index
      sources
    end

    def self.cache
      sources
    end
    SourceIndex = Specification
    class SourceList
      def search(*args); []; end
      def each(&block); end
      include Enumerable
    end
  end
end
