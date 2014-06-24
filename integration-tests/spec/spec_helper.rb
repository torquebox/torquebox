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

CORE_DIR = "#{File.dirname(__FILE__)}/../../core"
WEB_DIR = "#{File.dirname(__FILE__)}/../../web"
$: << "#{CORE_DIR}/lib"
$: << "#{WEB_DIR}/lib"
require "#{CORE_DIR}/spec/spec_helper"

require 'net/http'
require 'rexml/document'
require 'tmpdir'
require 'uri'

require 'torquebox-web'

require 'capybara/poltergeist'
require 'capybara/rspec'
require 'torquespec/torquespec'
require 'torquespec/server'

Capybara.app_host = "http://localhost:8080"
Capybara.run_server = false
Capybara.default_driver = :poltergeist

RSpec.configure do |config|

  config.before(:suite) do
    begin
      Capybara.visit "/"
    rescue Exception => ex
      if ex.message.include?('phantomjs')
        $stderr.puts <<-EOF



========================================================================

It looks like phantomjs was not found. Ensure it is installed and
available in your $PATH. See http://phantomjs.org/download.html for
details.

========================================================================



EOF
        $stderr.puts ex.message
        exit 1
      else
        raise ex
      end
    end
    if wildfly?
      require 'jbundler/aether'
      config = JBundler::Config.new
      aether = JBundler::AetherRuby.new(config)
      aether.add_artifact("org.wildfly:wildfly-dist:zip:#{TorqueBox::WILDFLY_VERSION}")
      aether.resolve
      zip_path = aether.classpath_array.find { |dep| dep.include?('wildfly/wildfly-dist/') }
      unzip_path = File.expand_path('../pkg', File.dirname(__FILE__))
      wildfly_home = File.join(unzip_path, 'wildfly')
      unless File.exists?(wildfly_home)
        FileUtils.mkdir_p(unzip_path)
        Dir.chdir(unzip_path) do
          unzip(zip_path)
          original_dir = File.expand_path(Dir['wildfly-*' ].first)
          FileUtils.mv(original_dir, wildfly_home)
        end
        standalone_xml = "#{wildfly_home}/standalone/configuration/standalone.xml"
        doc = REXML::Document.new(File.read(standalone_xml))
        interfaces = doc.root.get_elements("//management-interfaces/*")
        interfaces.each { |i| i.attributes.delete('security-realm')}
        open(standalone_xml, 'w') do |file|
          doc.write(file, 4)
        end
      end
      FileUtils.rm_rf(Dir["#{wildfly_home}/standalone/log/*"])
      FileUtils.rm_rf(Dir["#{wildfly_home}/standalone/deployments/*"])
      TorqueSpec.configure do |config|
        config.jboss_home = wildfly_home
      end
      $wildfly = TorqueSpec::Server.new
      $wildfly.start(:wait => 120)
    end
  end

  config.after(:suite) do
    $wildfly.stop if $wildfly
  end

  config.before(:all) do
    if self.class.respond_to?(:server_options)
      __server_start(self.class.server_options)
    end

    org.projectodd.wunderboss.WunderBoss.log_level = 'ERROR'
  end

  config.after(:all) do
    if self.class.respond_to?(:server_options)
      __server_stop
    end
  end

end

def bin_dir
  File.join(CORE_DIR, 'bin')
end

def apps_dir
  File.join(File.dirname(__FILE__), '..', 'apps')
end

def jruby_jvm_opts
  "-J-XX:+TieredCompilation -J-XX:TieredStopAtLevel=1"
end

def uberjar?
  ENV['PACKAGING'] == 'jar'
end

def wildfly?
  ENV['WILDFLY'] == 'true'
end

def unzip(path)
  if windows?
    `jar.exe xf #{path}`
  else
    `jar xf #{path}`
  end
end

def windows?
  RbConfig::CONFIG['host_os'] =~ /mswin/
end

def torquebox(options)
  path = options['--context-path'] || '/'
  app_dir = options['--dir']
  if uberjar?
    jarfile = "#{app_dir}/#{File.basename(app_dir)}.jar"
    before = lambda {
      command = "cd #{app_dir} && #{jruby_command} #{jruby_jvm_opts} -r 'bundler/setup' #{File.join(bin_dir, 'torquebox')} jar -v"
      if wildfly?
        name = path == '/' ? 'ROOT.jar' : "#{path.sub('/', '')}.jar"
        command << " --name #{name}"
        jarfile = "#{app_dir}/#{name}"
      end
      jar_output = `#{command}`
      puts jar_output if ENV['DEBUG']
      $wildfly.deploy(jarfile) if wildfly?
    }
    after = lambda {
      FileUtils.rm_f(jarfile)
    }
    command_prefix = "java -jar #{jarfile}"
  else
    before = nil
    after = nil
    command_prefix = "#{jruby_command} #{jruby_jvm_opts} -r 'bundler/setup' #{File.join(bin_dir, 'torquebox')} run"
  end
  args = options.to_a.flatten.join(' ')
  command = "#{command_prefix} #{ENV['DEBUG'] ? '-v' : '-q'} #{args}"
  metaclass = class << self; self; end
  metaclass.send(:define_method, :server_options) do
    return {
      :app_dir => app_dir,
      :path => path,
      :port => options['--port'] || '8080',
      :before => before,
      :after => after,
      :command => command
    }
  end
end

def rackup(options)
  metaclass = class << self; self; end
  metaclass.send(:define_method, :server_options) do
    app_dir = options.delete(:dir)
    args = options.to_a.flatten.join(' ')
    return {
      :app_dir => app_dir,
      :path => '/',
      :port => options['--port'] || '9292',
      :command => "#{jruby_command} #{jruby_jvm_opts} -r 'bundler/setup' -S rackup -s torquebox #{args} -O Quiet #{app_dir}/config.ru"
    }
  end
end

def __server_start(options)
  app_dir = options[:app_dir]
  path = options[:path]
  port = wildfly? ? '8080' : options[:port]
  Capybara.app_host = "http://localhost:#{port}"
  ENV['BUNDLE_GEMFILE'] = "#{app_dir}/Gemfile"
  if options[:before]
    options[:before].call
  end
  @server_after = options[:after]
  ENV['RUBYLIB'] = app_dir
  if wildfly?
    # app already depoyed in the before callback
  else
    pid, stdin, stdout, stderr = IO.popen4(options[:command])
    ENV['BUNDLE_GEMFILE'] = nil
    ENV['RUBYLIB'] = nil
    @server_pid = pid

    stdin.close
    stdout.sync = true
    stderr.sync = true
    error_seen = false
    @stdout_thread = Thread.new(stdout) { |stdout_io|
      begin
        while true
          STDOUT.write(stdout_io.readpartial(1024))
        end
      rescue EOFError
      end
    }
    @stderr_thread = Thread.new(stderr) { |stderr_io|
      begin
        while true
          STDERR.write(stderr_io.readpartial(1024))
          error_seen = true
        end
      rescue EOFError
      end
    }
  end
  start = Time.now
  booted = false
  timeout = 180
  last_exception = nil
  while (Time.now - start) < timeout do
    uri = URI.parse("#{Capybara.app_host}#{path}")
    begin
      response = Net::HTTP.get_response(uri)
      booted = true
      break
    rescue Exception => ex
      last_exception = ex
      sleep 0.2 # sleep and retry
    end
    break if error_seen
  end
  if !booted
    if last_exception && ENV['DEBUG']
      puts ex.inspect
      puts ex.backtrace
    end
    if ENV['DEBUG'] && @server_pid
      puts `jstack #{@server_pid}`
    end
    raise "Application #{app_dir} failed to start within #{timeout} seconds"
  end
end

def __server_stop
  if wildfly? && @jarfile
    $wildfly.undeploy(@jarfile)
  end
  if @server_pid
    begin
      Process.kill 'INT', @server_pid
    rescue Errno::ESRCH
      # ignore no such process errors - it died already
    end
    @server_pid = nil
    @stdout_thread.join
    @stdout_thread = nil
    @stderr_thread.join
    @stderr_thread = nil
  end
  if @server_after
    @server_after.call
    @server_after = nil
  end
end
