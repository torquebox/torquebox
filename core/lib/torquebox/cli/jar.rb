# Copyright 2014 Red Hat, Inc, and individual contributors.
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

require 'fileutils'
require 'pathname'
require 'rbconfig'
require 'tmpdir'
require 'torquebox-core'

module TorqueBox
  class CLI
    class Jar

      DEFAULT_INIT = "require 'torquebox-core'; \
        TorqueBox::CLI::Archive.new(ARGV).run;"

      def initialize
        @logger = org.projectodd.wunderboss.WunderBoss.logger('TorqueBox')
        @classpath = []
      end

      def usage_parameters
        "[options]"
      end

      def option_defaults
        {
          :destination => '.',
          :jar_name => "#{File.basename(Dir.pwd)}.jar",
          :include_jruby => true,
          :bundle_gems => true,
          :bundle_without => %W(development test assets),
          :rackup => 'config.ru'
        }
      end

      def available_options
        defaults = option_defaults
        [{
           :name => :destination,
           :switch => '--destination PATH',
           :description => "Destination directory for the jar file (default: #{defaults[:destination]})"
         },
         {
           :name => :jar_name,
           :switch => '--name NAME',
           :description => "Name of the jar file (default: #{defaults[:jar_name]})"
         },
         {
           :name => :include_jruby,
           :switch => '--[no-]include-jruby',
           :description => "Include JRuby in the jar (default: #{defaults[:include_jruby]})"
         },
         {
           :name => :bundle_gems,
           :switch => '--[no-]bundle-gems',
           :description => "Bundle gem dependencies in the jar (default: #{defaults[:bundle_gems]})"
         },
         {
           :name => :bundle_without,
           :switch => '--bundle-without GROUPS',
           :description => "Bundler groups to skip (default: #{defaults[:bundle_without]})",
           :type => Array
         },
         {
           :name => :main,
           :switch => '--main MAIN',
           :description => 'File to require to bootstrap the application (if not given, assumes a web app)'
         },
         {
           :name => :exclude,
           :switch => '--exclude EXCLUDES',
           :description => 'File paths to exclude from bundled jar',
           :type => Array
         }]
      end

      def setup_parser(parser, options)
        available_options.each do |opt|
          parser.on(*(opt.values_at(:short, :switch, :type, :description).compact)) do |arg|
            options[opt[:name]] = arg
          end
        end
        parser.on('--envvar KEY=VALUE',
                  'Specify an environment variable to set before running the app') do |arg|
          key, value = arg.split('=')
          if key.nil? || value.nil?
            $stderr.puts "Error: Environment variables must be separated by '='"
            exit 1
          end
          options[:envvar] ||= {}
          options[:envvar][key] = value
        end
      end

      def run(_argv, options)
        options = option_defaults.merge(options)
        jar_path = File.expand_path(File.join(options[:destination], options[:jar_name]))
        @logger.debug("Creating jar with options {}", options.inspect)

        init = DEFAULT_INIT
        if options[:main]
          init = "ENV['TORQUEBOX_MAIN'] = '#{options[:main]}'; #{init}"
        end
        init = "ENV['TORQUEBOX_RACKUP'] = '#{options[:rackup]}'; #{init}"

        jar_builder = org.torquebox.core.JarBuilder.new
        jar_builder.add_manifest_attribute("Main-Class", "org.torquebox.core.TorqueBoxMain")
        jar_builder.add_string(TorqueBox::JAR_MARKER, "")

        add_torquebox_files(jar_builder)

        if options[:include_jruby]
          add_jruby_files(jar_builder)
        end

        add_app_files(jar_builder, options)

        if options[:bundle_gems]
          tmpdir = Dir.mktmpdir("tmptorqueboxjar", ".")
          add_bundler_files(jar_builder, tmpdir, options[:bundle_without])
        end

        jar_builder.add_string("META-INF/app.properties",
                               app_properties(options[:envvar] || {}, init))

        if File.exist?(jar_path)
          @logger.info("Removing {}", jar_path)
          FileUtils.rm_f(jar_path)
        end
        @logger.info("Writing {}", jar_path)
        jar_builder.create(jar_path)
        jar_path
      ensure
        FileUtils.rm_rf(tmpdir) if tmpdir
      end

      def add_jruby_files(jar_builder)
        @logger.trace("Adding JRuby files to jar...")
        rb_config = RbConfig::CONFIG
        add_files(jar_builder,
                  :file_prefix => rb_config["prefix"],
                  :pattern => "/*",
                  :jar_prefix => "jruby")
        add_files(jar_builder,
                  :file_prefix => rb_config["libdir"],
                  :pattern => "/**/*",
                  :jar_prefix => "jruby/lib",
                  :exclude => ["jruby.jar", "ruby/gems/shared"])
        add_files(jar_builder,
                  :file_prefix => rb_config["bindir"],
                  :pattern => "/*",
                  :jar_prefix => "jruby/bin")
        add_jar(jar_builder, "#{rb_config['libdir']}/jruby.jar")
        add_default_gems(jar_builder)
      end

      def add_default_gems(jar_builder)
        default_spec_suffix = "specifications/default"
        default_spec_dir = "#{Gem.default_dir}/#{default_spec_suffix}"
        jar_prefix = "jruby/lib/ruby/gems/shared"
        Dir.glob("#{default_spec_dir}/*.gemspec").each do |file|
          jar_builder.add_file(File.join(jar_prefix, default_spec_suffix,
                                         File.basename(file)), file)
          gem_full_name = File.basename(file, ".*")
          add_files(jar_builder,
                    :file_prefix => "#{Gem.default_dir}/gems/#{gem_full_name}",
                    :pattern => "/**/*",
                    :jar_prefix => "#{jar_prefix}/gems/#{gem_full_name}")
        end
      end

      def add_app_files(jar_builder, options)
        @logger.trace("Adding application files to jar...")
        exclude = [Regexp.new("^[^/]*\.(jar|war)")]
        if options[:exclude]
          exclude += options[:exclude].map { |e| Regexp.new("^#{e}") }
        end
        add_files(jar_builder,
                  :file_prefix => Dir.pwd,
                  :pattern => "/**/{*,\.*manifest*}",
                  :jar_prefix => "app",
                  :exclude => exclude)
      end

      def add_bundler_files(jar_builder, tmpdir, bundle_without)
        @logger.trace("Adding bundler files to jar...")
        unless File.exist?(ENV['BUNDLE_GEMFILE'] || 'Gemfile')
          @logger.info("No Gemfile found - skipping gem dependencies")
          return {}
        end
        @logger.info("Bundling gem dependencies")
        require 'bundler'

        copy_bundle_config(tmpdir)

        vendor_dir_exists = File.exist?('vendor')
        cache_dir_exists = File.exist?('vendor/cache')
        bundle_dir_exists = File.exist?('vendor/bundle')
        already_cached = Dir.glob('vendor/cache/*.gem').count > 0
        already_bundled = if Bundler.settings.path.is_a?(String)
          Pathname.new(Bundler.settings.path).relative?
        else
          Pathname.new(Bundler.settings.path.path).relative?
        end

        lockfile = Bundler.default_lockfile
        original_lockfile = File.exist?(lockfile) ? File.read(lockfile) : nil

        cache_gems(tmpdir) unless already_cached
        bundle_gems(tmpdir, bundle_without) unless already_bundled
        copy_cached_gems(tmpdir) unless already_cached
        copy_and_restore_lockfile(tmpdir, lockfile, original_lockfile)

        add_files(jar_builder,
                  :file_prefix => tmpdir,
                  :pattern => "/{**/*,.bundle/**/*}",
                  :jar_prefix => "app",
                  :exclude => TorqueBox::Jars.list.map { |j| File.basename(j) })
        Gem.path.each do |prefix|
          add_files(jar_builder,
                    :file_prefix => prefix,
                    :pattern => "/**/bundler-#{Bundler::VERSION}{*,/**/*}",
                    :jar_prefix => "jruby/lib/ruby/gems/shared")
        end
      ensure
        FileUtils.rm_rf('vendor/bundle') unless bundle_dir_exists
        FileUtils.rm_rf('vendor/cache') unless cache_dir_exists
        FileUtils.rm_rf('vendor') unless vendor_dir_exists
      end

      def copy_bundle_config(tmpdir)
        if File.exist?('.bundle/config')
          FileUtils.mkdir_p("#{tmpdir}/.bundle")
          FileUtils.cp('.bundle/config', "#{tmpdir}/.bundle")
        end
      end

      def cache_gems(tmpdir)
        eval_in_new_ruby <<-EOS
          ENV['BUNDLE_APP_CONFIG'] = "#{tmpdir}/.bundle"
          require 'bundler/cli'
          Bundler::CLI.start(['cache', '--all'])
        EOS
      end

      def bundle_gems(tmpdir, bundle_without)
        install_options = %W(--local --path vendor/bundle --no-cache)
        unless bundle_without.empty?
          install_options += %W(--without #{bundle_without.join(' ')})
        end
        eval_in_new_ruby <<-EOS
          ENV['BUNDLE_APP_CONFIG'] = "#{tmpdir}/.bundle"
          require 'bundler/cli'
          Bundler::CLI.start(['install'] + #{install_options.inspect})
        EOS
        FileUtils.mkdir_p("#{tmpdir}/vendor/bundle/jruby")
        FileUtils.cp_r('vendor/bundle/jruby', "#{tmpdir}/vendor/bundle/")
      end

      def copy_cached_gems(tmpdir)
        FileUtils.mkdir_p("#{tmpdir}/vendor/cache")
        FileUtils.cp_r('vendor/cache', "#{tmpdir}/vendor/")
      end

      def copy_and_restore_lockfile(tmpdir, lockfile, original_lockfile)
        new_lockfile = File.exist?(lockfile) ? File.read(lockfile) : nil
        FileUtils.cp(lockfile, "#{tmpdir}/Gemfile.lock") if new_lockfile
        if original_lockfile.nil? && !new_lockfile.nil?
          FileUtils.rm_f(lockfile)
        elsif original_lockfile != new_lockfile
          File.open(lockfile, 'w') { |f| f.write(original_lockfile) }
        end
      end

      def add_jar(jar_builder, jar)
        @logger.debug("Adding jar {}", jar)
        jar_name = "jars/#{File.basename(jar)}"
        @classpath << "${extract_root}/#{jar_name}"
        jar_builder.add_file(jar_name, jar)
      end

      def add_torquebox_files(jar_builder)
        TorqueBox::Jars.list.each do |jar|
          if File.basename(jar) =~ /^wunderboss-(rack|ruby).*?\.jar$/
            add_jar(jar_builder, jar)
          else
            @logger.debug("Shading jar {}", jar)
            jar_builder.shade_jar(jar)
          end
        end
      end

      def add_files(jar_builder, options)
        prefix = options[:file_prefix]
        prefix += '/' unless prefix.end_with?('/')
        Dir.glob(File.join(prefix, options[:pattern])).each do |file|
          suffix = file.sub(prefix, '')
          excludes = [options[:exclude]].compact.flatten
          next if excludes.any? do |exclude|
            if exclude.is_a?(Regexp)
              exclude =~ suffix
            else
              suffix.include?(exclude)
            end
          end
          next if suffix.include?("tmptorqueboxjar")
          in_jar_name = options[:jar_prefix] ? File.join(options[:jar_prefix], suffix) : suffix
          jar_builder.add_file(in_jar_name, file)
        end
      end

      def eval_in_new_ruby(script)
        # Copy our environment to the new Ruby runtime
        config = org.jruby.RubyInstanceConfig.new
        config.environment = ENV
        config.current_directory = Dir.pwd
        ruby = org.jruby.Ruby.new_instance(config)
        unless %W(DEBUG TRACE).include?(TorqueBox::Logger.log_level)
          dev_null = PLATFORM =~ /mswin/ ? 'NUL' : '/dev/null'
          ruby.evalScriptlet("$stdout = File.open('#{dev_null}', 'w')")
        end
        ruby.evalScriptlet(script)
      end

      def app_properties(env, init)
        env_str = env.map do |key, value|
          "ENV['#{key}'] ||= '#{value}';"
        end.join(' ')
        classpath_str = @classpath.join(':')

        <<-EOS
language=ruby
extract_paths=app/:jruby/:jars/
root=${extract_root}/app
classpath=#{classpath_str}
init=ENV['BUNDLE_GEMFILE'] = nil; \
#{env_str} \
require "bundler/setup"; \
#{init}; \
require "torquebox/spec_helpers"; \
TorqueBox::SpecHelpers.booted
EOS
      end
    end
  end
end

TorqueBox::CLI.register_extension('jar', TorqueBox::CLI::Jar.new,
                                  'Create an executable jar from an application')
