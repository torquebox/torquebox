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
require 'rbconfig'
require 'tmpdir'
require 'torquebox-core'

module TorqueBox
  class CLI
    class FatJar

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
        zip_entries = {}
        if options['include_jruby']
          zip_entries.merge!(jruby_files)
        end
        zip_entries.merge!(app_files(jar_name))
        if options['bundle_gems']
          tmpdir = Dir.mktmpdir(nil, ".")
          zip_entries.merge!(bundler_files(tmpdir, options['bundle_without']))
        end
        if File.exists?(jar_name)
          @logger.infof("Removing %s", jar_name)
          FileUtils.rm_f(jar_name)
        end
        @logger.infof("Writing %s", jar_name);
        org.torquebox.core.JarBuilder.create(jar_name, zip_entries)
      ensure
        FileUtils.rm_rf(tmpdir) if options['bundle_gems']
      end

      def jruby_files
        @logger.tracef("Adding JRuby files to jar...")
        files = {}
        rb_config = RbConfig::CONFIG
        files.merge!(files_to_jar(:file_prefix => rb_config["prefix"],
                                  :pattern => "/*",
                                  :jar_prefix => "jruby"))
        files.merge!(files_to_jar(:file_prefix => rb_config["libdir"],
                                  :pattern => "/**/*",
                                  :jar_prefix => "jruby/lib",
                                  :exclude => "ruby/gems/shared"))
        files.merge!(files_to_jar(:file_prefix => rb_config["bindir"],
                                  :pattern => "/*",
                                  :jar_prefix => "jruby/bin"))
        files
      end

      def app_files(jar_name)
        @logger.tracef("Adding application files to jar...")
        files_to_jar(:file_prefix => Dir.pwd,
                     :pattern => "/**/*",
                     :jar_prefix => "app",
                     :exclude => jar_name)
      end

      def bundler_files(tmpdir, bundle_without)
        @logger.tracef("Adding bundler files to jar...")
        unless File.exists?(ENV['BUNDLE_GEMFILE'] || 'Gemfile')
          @logger.infof("No Gemfile found - skipping gem dependencies")
          return {}
        end
        @logger.infof("Bundling gem dependencies")
        require 'bundler'
        gemfile = Bundler.default_gemfile
        lockfile = Bundler.default_lockfile
        FileUtils.cp(gemfile, "#{tmpdir}/Gemfile")
        FileUtils.cp(lockfile, "#{tmpdir}/Gemfile.lock")
        eval_in_new_ruby <<-EOS
          Dir.chdir('#{tmpdir}')
          require 'bundler/cli'
          Bundler::CLI.start(['cache', '--all'])
        EOS
        install_options = %w(--local --path vendor/bundle --no-cache)
        unless bundle_without.empty?
          install_options += %W(--without #{bundle_without.join(' ')})
        end
        eval_in_new_ruby <<-EOS
          Dir.chdir('#{tmpdir}')
          require 'bundler/cli'
          Bundler::CLI.start(['install'] + #{install_options.inspect})
        EOS
        files = {}
        files.merge!(files_to_jar(:file_prefix => tmpdir,
                                  :pattern => "/{**/*,.bundle/**/*}",
                                  :jar_prefix => "app"))
        Gem.default_path.each do |prefix|
          files.merge!(files_to_jar(:file_prefix => prefix,
                                    :pattern => "/**/bundler-#{Bundler::VERSION}{*,/**/*}",
                                    :jar_prefix => "jruby/lib/ruby/gems/shared"))
        end
        files
      end

      def files_to_jar(options)
        entries = {}
        prefix = options[:file_prefix]
        Dir.glob("#{prefix}#{options[:pattern]}").each do |file|
          suffix = file.sub(prefix, '')
          next if options[:exclude] && suffix.include?(options[:exclude])
          entries[File.join(options[:jar_prefix], suffix)] = file
        end
        entries
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

TorqueBox::CLI.register_extension('fatjar', TorqueBox::CLI::FatJar.new,
                                  'Create an executable "fatjar"')
