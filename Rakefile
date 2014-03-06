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
RSpec::Core::RakeTask.new(:spec)
task :default => :spec


WB_JARS_DIR = 'lib/wunderboss-jars'
require 'rake/javaextensiontask'
Rake::JavaExtensionTask.new('wunderboss-torquebox') do |ext|
  ext.classpath = JBUNDLER_CLASSPATH
  ext.source_version = '1.7'
  ext.target_version = '1.7'
end
task 'compile' do
  FileUtils.mkdir_p(WB_JARS_DIR)
  excluded_jars = ['jruby-complete']
  JBUNDLER_CLASSPATH.each do |file|
    if file.end_with?('.jar')
      jar = File.basename(file)
      unless excluded_jars.any? { |excluded_jar| jar.start_with?(excluded_jar) }
        install file, "#{WB_JARS_DIR}/#{jar}"
      end
    end
  end
end
task 'build' => 'compile'
# Hide the clobber task from -T and remove its dependency on clean
Rake::Task[:clobber].clear_comments
Rake::Task[:clobber].clear_prerequisites


require 'fileutils'
desc 'Clean the pkg directory'
task 'clean' => 'clobber' do
  FileUtils.rm_rf(File.join(Dir.pwd, 'pkg'))
  FileUtils.rm_rf(File.join(Dir.pwd, WB_JARS_DIR))
end
