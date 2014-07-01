# Copyright 2008-2013 Red Hat, Inc, and individual contributors.
#
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
#
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this software; if not, write to the Free
# Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA, or see the FSF site: http://www.fsf.org.

require 'fileutils'
require 'pathname'
require 'rbconfig'
require 'tmpdir'
require 'torquebox-core'

module TorqueBox
  class CLI
    class Jar

      DEFAULT_OPTIONS = {
        'jar_name' => "#{File.basename(Dir.pwd)}.jar",
        'include_jruby' => true,
        'bundle_gems' => true,
        'bundle_without' => ['development', 'test', 'assets']
      }

      def initialize
        @logger = org.projectodd.wunderboss.WunderBoss.logger('TorqueBox')
      end

      def usage_parameters
        "[options]"
      end

      def setup_parser(parser, options)
        parser.on('--name NAME',
                  "Name of the jar file (default: #{DEFAULT_OPTIONS['jar_name']})") do |arg|
          options['jar_name'] = arg
        end
        parser.on('--[no-]include-jruby',
                  "Include JRuby in the jar (default: #{DEFAULT_OPTIONS['include_jruby']})") do |arg|
          options['include_jruby'] = arg
        end
        parser.on('--[no-]bundle-gems',
                  "Bundle gem dependencies in the jar (default: #{DEFAULT_OPTIONS['bundle_gems']})") do |arg|
          options['bundle_gems'] = arg
        end
        parser.on('--bundle-without GROUPS', Array,
                  "Bundler groups to skip (default: #{DEFAULT_OPTIONS['bundle_without'].join(', ')})") do |arg|
        end
      end

      def run(argv, options)
        options = DEFAULT_OPTIONS.merge(options)
        jar_name = options['jar_name']
        @logger.debugf("Creating jar with options %s", options.inspect)

        jar_builder = org.torquebox.core.JarBuilder.new
        jar_builder.add_manifest_attribute("Main-Class", "org.torquebox.core.TorqueBoxMain")

        app_properties = <<-EOS
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
  TorqueBox::Web::Server.run("default"); \
end
EOS
        jar_builder.add_string("META-INF/app.properties", app_properties)
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

        if File.exists?(jar_name)
          @logger.infof("Removing %s", jar_name)
          FileUtils.rm_f(jar_name)
        end
        @logger.infof("Writing %s", jar_name);
        jar_builder.create(jar_name)
      ensure
        FileUtils.rm_rf(tmpdir) if options['bundle_gems']
      end

      def add_jruby_files(jar_builder)
        @logger.tracef("Adding JRuby files to jar...")
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
        @logger.tracef("Adding application files to jar...")
        add_files(jar_builder,
                  :file_prefix => Dir.pwd,
                  :pattern => "/**/*",
                  :jar_prefix => "app",
                  :exclude => jar_name)
      end

      def add_bundler_files(jar_builder, tmpdir, bundle_without)
        @logger.tracef("Adding bundler files to jar...")
        unless File.exists?(ENV['BUNDLE_GEMFILE'] || 'Gemfile')
          @logger.info("No Gemfile found - skipping gem dependencies")
          return {}
        end
        @logger.info("Bundling gem dependencies")
        require 'bundler'

        if File.exists?('.bundle/config')
          FileUtils.mkdir_p("#{tmpdir}/.bundle")
          FileUtils.cp('.bundle/config', "#{tmpdir}/.bundle")
        end

        ###
        # ditch copying to tmp dir:
        # set BUNDLE_APP_CONFIG, remove vendor/cache unless it previously existed
        ###

        vendor_dir_exists = File.exists?('vendor')
        cache_dir_exists = File.exists?('vendor/cache')
        bundle_dir_exists = File.exists?('vendor/bundle')
        already_cached = Dir.glob('vendor/cache/*.gem').count > 0
        Bundler.settings.path
        # TODO: Use Bundler.settings.path to check if already bundled,
        # perhaps by looking for a relative path already_bundled =
        already_bundled = Pathname.new(Bundler.settings.path).relative?

        unless already_cached
          eval_in_new_ruby <<-EOS
            ENV['BUNDLE_APP_CONFIG'] = "#{tmpdir}/.bundle"
            require 'bundler/cli'
            Bundler::CLI.start(['cache', '--all'])
          EOS
        end

        unless already_bundled
          install_options = %w(--local --path vendor/bundle --no-cache)
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

        unless already_cached
          FileUtils.mkdir_p("#{tmpdir}/vendor/cache")
          FileUtils.cp_r('vendor/cache', "#{tmpdir}/vendor/")
        end

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

      def add_torquebox_files(jar_builder)
        TorqueBox::Jars.list.each do |jar|
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
        if !['DEBUG', 'TRACE'].include?(TorqueBox::Logger.log_level)
          dev_null = PLATFORM =~ /mswin/ ? 'NUL' : '/dev/null'
          ruby.evalScriptlet("$stdout = File.open('#{dev_null}', 'w')")
        end
        ruby.evalScriptlet(script)
      end
    end
  end
end

TorqueBox::CLI.register_extension('jar', TorqueBox::CLI::Jar.new,
                                  'Create an executable and deployable jar from an application')
