require 'bundler'
require 'fileutils'

# Don't complain about missing files when cleaning
require 'rake/clean'
module Rake
  module Cleaner
    module_function
    def cleanup(file_name, opts={})
      begin
        # rm_r -> rm_rf here
        rm_rf file_name, opts
      rescue StandardError => ex
        puts "Failed to remove #{file_name}: #{ex}"
      end
    end
  end
end

module TorqueBox
  class RakeHelper
    extend Rake::DSL if defined? Rake::DSL

    class << self
      def install_standard_tasks
        install_bundler_tasks
        install_rspec_tasks
        install_clean_tasks
      end

      # Install Rake tasks necessary to compile TorqueBox gems
      #
      # @param options Optional parameters (a Hash), including:
      # @option options [String] :source The directory containing java code to compile, if any
      # @option options [String] :gemspec The path to a gemspec file containing requirements entries for any maven jars required for compilation
      # @option options [String] :copy_deps The path to copy maven dependencies to - nil means don't copy
      # @option options [Array] :excluded_deps An array of maven dependencies to exclude from inclusion in the gem
      def install_java_tasks(options={})
        #
        # Resolve maven dependencies using JBundler API
        # We don't use JBundler directly because that requires a separate
        # 'jbundle install' step to get started.
        #
        if options[:gemspec]
          gemspec = eval(File.read(options[:gemspec]))
          jars = []
          gemspec.requirements.each do |requirements|
            requirements.split(/\n/).each do |requirement|
              if requirement =~ /^\s*jar\s/
                coordinate = requirement.sub(/^\s*jar\s/, '')
                coordinate = coordinate.split(',').map(&:strip).join(':')
                jars << coordinate
              end
            end
          end
          require 'jbundler/aether'
          config = JBundler::Config.new
          aether = JBundler::AetherRuby.new(config)
          aether.add_repository('bees-incremental', 'https://repository-projectodd.forge.cloudbees.com/incremental/')
          aether.add_repository('jboss', 'http://repository.jboss.org/nexus/content/groups/public/')
          jars.each { |jar| aether.add_artifact(jar) }
          aether.resolve
          classpath = aether.classpath_array
        else
          classpath = []
        end

        if options[:source]
          require 'rake/javaextensiontask'
          ext_task = Rake::JavaExtensionTask.new(options[:source]) do |ext|
            ext.classpath = classpath
            ext.source_version = '1.7'
            ext.target_version = '1.7'
          end
        end
        if options[:copy_deps]
          deps_dir = options[:copy_deps]
          directory deps_dir
          task 'compile' => deps_dir
          excluded_deps = options[:excluded_deps] || []
          classpath.each do |path|
            if path.end_with?('.jar')
              jar = File.basename(path)
              unless excluded_deps.any? { |excluded_dep| jar.match(excluded_dep) }
                file "#{deps_dir}/#{jar}" do
                  install path, "#{deps_dir}/#{jar}"
                end
                task 'compile' => "#{deps_dir}/#{jar}"
              end
            end
          end
          task 'clean' do
            FileUtils.rm_rf(File.join(Dir.pwd, deps_dir))
          end
        end
        task 'build' => 'compile'
        task 'spec' => 'compile'
      end

      def install_bundler_tasks
        Bundler::GemHelper.install_tasks
      end

      def install_rspec_tasks
        require 'rspec/core/rake_task'
        RSpec::Core::RakeTask.new(:spec) do |config|
          # Ignore subdirectories when looking for specs
          config.pattern = './spec/*_spec.rb'
        end
      end

      def install_clean_tasks
        # Hide the clobber task from -T and remove its dependency on clean
        Rake::Task[:clobber].clear_comments
        Rake::Task[:clobber].clear_prerequisites
        task 'clean' => 'clobber' do
          FileUtils.rm_rf(File.join(Dir.pwd, 'pkg'))
        end
      end
    end
  end
end
