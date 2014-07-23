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
        standalone_xml = "#{wildfly_home}/standalone/configuration/standalone-full.xml"
        doc = REXML::Document.new(File.read(standalone_xml))
        interfaces = doc.root.get_elements("//management-interfaces/*")
        interfaces.each { |i| i.attributes.delete('security-realm')}
        hornetq = doc.root.get_elements("//hornetq-server").first
        hornetq.add_element('journal-type').text = 'NIO'
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
  if !ENV['DEBUG']
    dev_null = PLATFORM =~ /mswin/ ? 'NUL' : '/dev/null'
    ruby.evalScriptlet("$stdout = File.open('#{dev_null}', 'w')")
  end
  ruby.evalScriptlet(script)
  ruby.tearDown(false)
end

ALREADY_BUNDLED = []
def bundle_install(app_dir)
  if !ALREADY_BUNDLED.include?(app_dir) && File.exists?("#{app_dir}/Gemfile")
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
    jarfile = "#{app_dir}/#{File.basename(app_dir)}.jar"
    before = lambda {
      command = "cd #{app_dir} && #{jruby_command} #{jruby_jvm_opts} -r 'bundler/setup' #{File.join(bin_dir, 'torquebox')}"
      if wildfly?
        name = path == '/' ? 'ROOT.war' : "#{path.sub('/', '')}.war"
        command << " war -v --name #{name}"
        jarfile = "#{app_dir}/#{name}"
      else
        command << " jar -v"
      end
      jar_output = `#{command}`
      puts jar_output if ENV['DEBUG']
      $wildfly.deploy(jarfile) if wildfly?
    }
    after = lambda {
      $wildfly.undeploy(jarfile) if wildfly?
      FileUtils.rm_f(jarfile)
      FileUtils.rm_f("#{app_dir}/#{File.basename(app_dir)}.jar")
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

def embedded(command, options)
  metaclass = class << self; self; end
  metaclass.send(:define_method, :server_options) do
    app_dir = options.delete(:dir)
    return {
      :app_dir => app_dir,
      :chdir => app_dir,
      :path => '/',
      :port => options['--port'] || '8080',
      :command => "#{jruby_command} #{jruby_jvm_opts} -r 'bundler/setup' #{command}"
    }
  end
end

def __server_start(options)
  app_dir = options[:app_dir]
  chdir = options[:chdir]
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
    if chdir
      @old_pwd = Dir.pwd
      Dir.chdir(chdir)
    end
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
end
