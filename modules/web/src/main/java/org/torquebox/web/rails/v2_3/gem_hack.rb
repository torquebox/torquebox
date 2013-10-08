# Rails 2 and RubyGems shipped with JRuby 1.7.5 don't play together
if JRUBY_VERSION >= '1.7.5'
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
