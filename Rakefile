require 'bundler'
require 'jbundler'
module Bundler
  class GemHelper

    def version_tag
      "vtorqbox-#{version}"
    end
  end
end
Bundler::GemHelper.install_tasks


require 'rspec/core/rake_task'
RSpec::Core::RakeTask.new(:spec) do |config|
  config.pattern = './spec/*_spec.rb'
end
task :default => :spec


require 'rake/javaextensiontask'
Rake::JavaExtensionTask.new('wunderboss-torquebox') do |ext|
  ext.classpath = JBUNDLER_CLASSPATH
  ext.source_version = '1.7'
  ext.target_version = '1.7'
end
WB_JARS_DIR = 'lib/wunderboss-jars'
directory WB_JARS_DIR
task 'compile' => WB_JARS_DIR do
end
excluded_jars = ['jruby-complete']
JBUNDLER_CLASSPATH.each do |path|
  if path.end_with?('.jar')
    jar = File.basename(path)
    unless excluded_jars.any? { |excluded_jar| jar.start_with?(excluded_jar) }
      file "#{WB_JARS_DIR}/#{jar}" => 'lib/wunderboss-torquebox.jar' do
        install path, "#{WB_JARS_DIR}/#{jar}"
      end
      task 'compile' => "#{WB_JARS_DIR}/#{jar}"
    end
  end
end
task 'build' => 'compile'
# Hide the clobber task from -T and remove its dependency on clean
Rake::Task[:clobber].clear_comments
Rake::Task[:clobber].clear_prerequisites
task 'spec' => 'compile'


require 'rake/clean'
module Rake
  module Cleaner
    module_function
    def cleanup(file_name, opts={})
      begin
        rm_rf file_name, opts # rm_rf to not complain about missing files
      rescue StandardError => ex
        puts "Failed to remove #{file_name}: #{ex}"
      end
    end
  end
end
require 'fileutils'
task 'clean' => 'clobber' do
  FileUtils.rm_rf(File.join(Dir.pwd, 'pkg'))
  FileUtils.rm_rf(File.join(Dir.pwd, WB_JARS_DIR))
end
