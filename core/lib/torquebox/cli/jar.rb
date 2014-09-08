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

      DEFAULTS = {
        'jar_name' => "#{File.basename(Dir.pwd)}.jar",
        'include_jruby' => true,
        'bundle_gems' => true,
        'bundle_without' => %W(development test assets)
      }

      APP_PROPERTIES = <<-EOS
language=ruby
extract_paths=app/:jruby/
root=${extract_root}/app
init=ENV['BUNDLE_GEMFILE'] = nil; \
require "bundler/setup"; \
require "torquebox-web"; \
if org.projectodd.wunderboss.WunderBoss.options.get("wildfly-service").nil?; \
  begin; \
    TorqueBox::CLI.new(ARGV.unshift("run")); \
  rescue SystemExit => e; \
    status = e.respond_to?(:status) ? e.status : 0; \
    java.lang.System.exit(status); \
  end; \
else; \
  TorqueBox::Web.run; \
end
EOS

      def initialize
        @logger = org.projectodd.wunderboss.WunderBoss.logger('TorqueBox')
      end

      def usage_parameters
        "[options]"
      end

      def setup_parser(parser, options)
        parser.on('--name NAME',
                  "Name of the jar file (default: #{DEFAULTS['jar_name']})") do |arg|
          options['jar_name'] = arg
        end
        parser.on('--[no-]include-jruby',
                  "Include JRuby in the jar (default: #{DEFAULTS['include_jruby']})") do |arg|
          options['include_jruby'] = arg
        end
        bundle_gems_default = DEFAULTS['bundle_gems']
        parser.on('--[no-]bundle-gems',
                  "Bundle gem dependencies in the jar (default: #{bundle_gems_default})") do |arg|
          options['bundle_gems'] = arg
        end
        bundle_without_default = DEFAULTS['bundle_without'].join(', ')
        parser.on('--bundle-without GROUPS', Array,
                  "Bundler groups to skip (default: #{bundle_without_default})") do |arg|
        end
      end

      def run(_argv, options)
        options = DEFAULTS.merge(options)
        jar_name = options['jar_name']
        @logger.debug("Creating jar with options {}", options.inspect)

        jar_builder = org.torquebox.core.JarBuilder.new
        jar_builder.add_manifest_attribute("Main-Class", "org.torquebox.core.TorqueBoxMain")
        jar_builder.add_string("META-INF/app.properties", APP_PROPERTIES)
        jar_builder.add_string(TorqueBox::JAR_MARKER, "")

        if options['include_jruby']
          add_jruby_files(jar_builder)
        end

        add_app_files(jar_builder, jar_name)

        if options['bundle_gems']
          tmpdir = Dir.mktmpdir("tmptorqueboxjar", ".")
          add_bundler_files(jar_builder, tmpdir, options['bundle_without'])
        end

        add_torquebox_files(jar_builder)

        if File.exist?(jar_name)
          @logger.info("Removing {}", jar_name)
          FileUtils.rm_f(jar_name)
        end
        @logger.info("Writing {}", jar_name)
        jar_builder.create(jar_name)
        jar_name
      ensure
        FileUtils.rm_rf(tmpdir) if options['bundle_gems']
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
        jar_builder.shade_jar("#{rb_config['libdir']}/jruby.jar")
      end

      def add_app_files(jar_builder, jar_name)
        @logger.trace("Adding application files to jar...")
        add_files(jar_builder,
                  :file_prefix => Dir.pwd,
                  :pattern => "/**/*",
                  :jar_prefix => "app",
                  :exclude => [jar_name, jar_name.sub('.jar', '.war')])
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
        already_bundled = Pathname.new(Bundler.settings.path).relative?

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
                  :exclude => TorqueBox::Jars.list.map { |j| File.basename(j)  })
        Gem.default_path.each do |prefix|
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

      def add_torquebox_files(jar_builder)
        TorqueBox::Jars.list.each do |jar|
          @logger.debug("Shading jar {}", jar)
          jar_builder.shade_jar(jar)
        end
      end

      def add_files(jar_builder, options)
        prefix = options[:file_prefix]
        Dir.glob("#{prefix}#{options[:pattern]}").each do |file|
          suffix = file.sub(prefix, '')
          excludes = [options[:exclude]].compact.flatten
          next if excludes.any? { |exclude| suffix.include?(exclude) }
          next if suffix.include?("tmptorqueboxjar")
          jar_builder.add_file(File.join(options[:jar_prefix], suffix), file)
        end
      end

      def eval_in_new_ruby(script)
        ruby = org.jruby.Ruby.new_instance
        unless %W(DEBUG TRACE).include?(TorqueBox::Logger.log_level)
          dev_null = PLATFORM =~ /mswin/ ? 'NUL' : '/dev/null'
          ruby.evalScriptlet("$stdout = File.open('#{dev_null}', 'w')")
        end
        ruby.evalScriptlet(script)
      end
    end
  end
end

TorqueBox::CLI.register_extension('jar', TorqueBox::CLI::Jar.new,
                                  'Create an executable jar from an application')
