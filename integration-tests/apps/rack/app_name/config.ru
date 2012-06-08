require 'rack_app.rb'

use Rack::Reloader
# Workaround Rack::Reloader & JRuby 1.6.7.2 bug:
# https://github.com/rack/rack/issues/391
module Rack
  class Reloader
    module Stat
      alias_method :safe_stat_original, :safe_stat
      def safe_stat(file)
        safe_stat_original(file)
      rescue Errno::ESRCH
        @cache.delete(file) and false
      end
    end
  end
end

run RackApp.new 