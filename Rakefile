require 'bundler'
require 'fileutils'

module Bundler
  class GemHelper

    def version_tag
      "vtorqbox-#{version}"
    end
  end
end
Bundler::GemHelper.install_tasks

require 'rspec/core/rake_task'
RSpec::Core::RakeTask.new(:spec)
task :default => :spec

desc 'Clean the pkg directory'
task 'clean' do
  FileUtils.rm_rf(File.join(Dir.pwd, 'pkg'))
end
