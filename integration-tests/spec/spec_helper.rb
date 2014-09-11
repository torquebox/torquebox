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

CORE_DIR = "#{File.dirname(__FILE__)}/../../core"
WEB_DIR = "#{File.dirname(__FILE__)}/../../web"
MESSAGING_DIR = "#{File.dirname(__FILE__)}/../../messaging"
$LOAD_PATH << "#{CORE_DIR}/lib"
$LOAD_PATH << "#{WEB_DIR}/lib"
$LOAD_PATH << "#{MESSAGING_DIR}/lib"
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
      wildfly_home = install_wildfly
      TorqueSpec.configure do |torquespec_config|
        torquespec_config.jboss_home = wildfly_home
      end
      Thread.current[:wildfly] = TorqueSpec::Server.new
      wildfly_server.start(:wait => 120)
    end
  end

  config.after(:suite) do
    wildfly_server.stop if wildfly_server
  end

  config.before(:all) do
    if self.class.respond_to?(:server_options)
      server_start(self.class.server_options)
    end

    org.projectodd.wunderboss.WunderBoss.log_level = 'ERROR'
  end

  config.after(:all) do
    if self.class.respond_to?(:server_options)
      server_stop
    end
  end

end

module TorqueSpec
  module AS7
    alias_method :old_start_command, :start_command
    def start_command
      old_start_command << " --server-config=standalone-full.xml"
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

def embedded?
  !wildfly?
end

def embedded_from_disk?
  embedded? && !uberjar?
end

def wildfly_server
  Thread.current[:wildfly]
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

def eval_in_new_ruby(script)
  ruby = org.jruby.Ruby.new_instance
  unless ENV['DEBUG']
    dev_null = PLATFORM =~ /mswin/ ? 'NUL' : '/dev/null'
    ruby.evalScriptlet("$stdout = File.open('#{dev_null}', 'w')")
  end
  ruby.evalScriptlet(script)
  ruby.tearDown(false)
end

ALREADY_BUNDLED = []
def bundle_install(app_dir)
  if !ALREADY_BUNDLED.include?(app_dir) && File.exist?("#{app_dir}/Gemfile")
    eval_in_new_ruby <<-EOS
      ENV['BUNDLE_GEMFILE'] = nil
      Dir.chdir('#{app_dir}')
      require 'bundler/cli'
      Bundler::CLI.start(['install'])
    EOS
    ALREADY_BUNDLED << app_dir
  end
end

def torquebox(options)
  path = options['--context-path'] || '/'
  app_dir = options['--dir']
  bundle_install(app_dir)
  if uberjar?
    before, after, command_prefix = uberjar(app_dir, path)
  else
    before = nil
    after = nil
    command_prefix = "#{jruby_command} #{jruby_jvm_opts} -r 'bundler/setup' "\
      "#{File.join(bin_dir, 'torquebox')} run"
  end
  args = options.to_a.flatten.join(' ')
  command = "#{command_prefix} #{ENV['DEBUG'] ? '-v' : '-q'} #{args}"
  metaclass = class << self; self; end
  metaclass.send(:define_method, :server_options) do
    return {
      :app_dir => app_dir,
      :port => options['--port'] || '8080',
      :before => before,
      :after => after,
      :command => command
    }
  end
end

def uberjar(app_dir, path, main = nil)
  jarfile = "#{app_dir}/#{File.basename(app_dir)}.jar"
  before = lambda do
    command = "cd #{app_dir} && #{jruby_command} #{jruby_jvm_opts} "\
    "-r 'bundler/setup' #{File.join(bin_dir, 'torquebox')}"
    if wildfly?
      name = path == '/' ? 'ROOT.war' : "#{path.sub('/', '')}.war"
      command << " war -v --name #{name} "
      marker_key = TorqueBox::SpecHelpers.boot_marker_env_key
      command << "--env #{marker_key}=#{ENV[marker_key]}"
      jarfile = "#{app_dir}/#{name}"
    else
      command << " jar -v"
    end
    command << " --main #{main}" if main
    jar_output = `#{command} 2>&1`
    puts jar_output if ENV['DEBUG']
    wildfly_server.deploy(jarfile) if wildfly?
  end
  after = lambda do
    wildfly_server.undeploy(jarfile) if wildfly?
    FileUtils.rm_f(jarfile)
    FileUtils.rm_f("#{app_dir}/#{File.basename(app_dir)}.jar")
  end
  command_prefix = "java -jar #{jarfile}"
  [before, after, command_prefix]
end

def rackup(options)
  metaclass = class << self; self; end
  metaclass.send(:define_method, :server_options) do
    app_dir = options.delete(:dir)
    args = options.to_a.flatten.join(' ')
    command = "#{jruby_command} #{jruby_jvm_opts} -r 'bundler/setup' "\
      "-S rackup -s torquebox #{args} -O Quiet #{app_dir}/config.ru"
    return {
      :app_dir => app_dir,
      :port => options['--port'] || '9292',
      :command => command
    }
  end
end

def embedded(main, options)
  app_dir = options[:dir]
  path = '/'
  if uberjar?
    before, after, command = uberjar(app_dir, path, main)
  else
    before = nil
    after = nil
    command = "#{jruby_command} #{jruby_jvm_opts} -r 'bundler/setup' #{main}"
  end
  metaclass = class << self; self; end
  metaclass.send(:define_method, :server_options) do
    return {
      :app_dir => app_dir,
      :chdir => app_dir,
      :port => options[:port] || '8080',
      :before => before,
      :after => after,
      :command => command
    }
  end
end

def server_start(options)
  app_dir = options[:app_dir]
  chdir = options[:chdir]
  port = wildfly? ? '8080' : options[:port]
  Capybara.app_host = "http://localhost:#{port}"
  ENV['BUNDLE_GEMFILE'] = "#{app_dir}/Gemfile"
  TorqueBox::SpecHelpers.set_boot_marker
  options[:before].call if options[:before]
  @server_after = options[:after]
  ENV['RUBYLIB'] = app_dir
  error_seen = Java::JavaUtilConcurrentAtomic::AtomicBoolean.new
  unless wildfly?
    if chdir
      @old_pwd = Dir.pwd
      Dir.chdir(chdir)
    end
    pid, stdin, stdout, stderr = IO.popen4(options[:command])
    ENV['BUNDLE_GEMFILE'] = ENV['RUBYLIB'] = nil
    @server_pid = pid
    @stdout_thread, @stderr_thread = pump_server_streams(stdin, stdout,
                                                         stderr, error_seen)
  end
  wait_for_boot(app_dir, 180, error_seen)
end

def pump_server_streams(stdin, stdout, stderr, error_seen)
  stdin.close
  stdout.sync = true
  stderr.sync = true
  stdout_thread = Thread.new(stdout) do |stdout_io|
    begin
      loop do
        STDOUT.write(stdout_io.readpartial(1024))
      end
    rescue EOFError
    end
  end
  stderr_thread = Thread.new(stderr) do |stderr_io|
    begin
      loop do
        STDERR.write(stderr_io.readpartial(1024))
        error_seen.set(true)
      end
    rescue EOFError
    end
  end
  [stdout_thread, stderr_thread]
end

def wait_for_boot(app_dir, timeout, error_seen)
  start = Time.now
  while (Time.now - start) < timeout
    booted = TorqueBox::SpecHelpers.booted?
    break if booted
    break if error_seen.get
    sleep 0.2 # sleep and retry
  end
  handle_boot_failure(app_dir, timeout, error_seen) unless booted
end

def handle_boot_failure(app_dir, timeout, error_seen)
  raise "Application failed to boot" if error_seen.get
  if ENV['DEBUG'] && @server_pid
    puts `jstack #{@server_pid}`
  end
  raise "Application #{app_dir} failed to start within #{timeout} seconds"
end

def server_stop
  if wildfly? && @jarfile
    wildfly_server.undeploy(@jarfile)
  end
  if @old_pwd
    Dir.chdir(@old_pwd)
    @old_pwd = nil
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
  TorqueBox::SpecHelpers.clear_boot_marker
end

def install_wildfly
  require 'jbundler/aether'
  config = JBundler::Config.new
  aether = JBundler::AetherRuby.new(config)
  aether.add_artifact("org.wildfly:wildfly-dist:zip:#{TorqueBox::WILDFLY_VERSION}")
  aether.resolve
  zip_path = aether.classpath_array.find { |dep| dep.include?('wildfly/wildfly-dist/') }
  unzip_path = File.expand_path('../pkg', File.dirname(__FILE__))
  wildfly_home = File.join(unzip_path, 'wildfly')
  unless File.exist?(wildfly_home)
    FileUtils.mkdir_p(unzip_path)
    Dir.chdir(unzip_path) do
      unzip(zip_path)
      original_dir = File.expand_path(Dir['wildfly-*'].first)
      FileUtils.mv(original_dir, wildfly_home)
    end
    standalone_xml = "#{wildfly_home}/standalone/configuration/standalone-full.xml"
    doc = REXML::Document.new(File.read(standalone_xml))
    interfaces = doc.root.get_elements("//management-interfaces/*")
    interfaces.each { |i| i.attributes.delete('security-realm') }
    hornetq = doc.root.get_elements("//hornetq-server").first
    hornetq.add_element('journal-type').text = 'NIO'
    hornetq.add_element('security-enabled').text = 'false'
    open(standalone_xml, 'w') do |file|
      doc.write(file, 4)
    end
  end
  FileUtils.rm_rf(Dir["#{wildfly_home}/standalone/log/*"])
  FileUtils.rm_rf(Dir["#{wildfly_home}/standalone/deployments/*"])
  wildfly_home
end
