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
$: << "#{CORE_DIR}/lib"
require "#{CORE_DIR}/spec/spec_helper"
require 'capybara/poltergeist'
require 'capybara/rspec'
require 'net/http'
require 'uri'

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
  end

  config.before(:all) do
    if self.class.respond_to?(:torquebox_options)
      __torquebox_start(self.class.torquebox_options)
    end

    org.projectodd.wunderboss.WunderBoss.log_level = 'ERROR'
  end

  config.after(:all) do
    if self.class.respond_to?(:torquebox_options)
      __torquebox_stop
    end
  end

end

def lib_dir
  File.join(File.dirname(__FILE__), '..', 'lib')
end

def bin_dir
  File.join(CORE_DIR, 'bin')
end

def apps_dir
  File.join(File.dirname(__FILE__), 'apps')
end

def torquebox(options)
  metaclass = class << self; self; end
  metaclass.send(:define_method, :torquebox_options) do
    return options
  end
end

def __torquebox_start(options)
  app_dir = options['--dir']
  context = options['--context-path'] || '/'
  port = options['--port'] || '8080'
  Capybara.app_host = "http://localhost:#{port}"
  ENV['BUNDLE_GEMFILE'] = "#{app_dir}/Gemfile"
  ENV['RUBYLIB'] = "#{lib_dir}:#{app_dir}"
  jruby_jvm_opts = "-J-XX:+TieredCompilation -J-XX:TieredStopAtLevel=1"
  args = options.to_a.flatten.join(' ')
  command = "#{jruby_command} #{jruby_jvm_opts} -r 'bundler/setup' -I#{CORE_DIR}/lib #{File.join(bin_dir, 'torquebox')} run -vv #{args}"
  pid, stdin, stdout, stderr = IO.popen4(command)
  ENV['BUNDLE_GEMFILE'] = nil
  ENV['RUBYLIB'] = nil
  @tb_pid = pid

  stdin.close
  stdout.sync = true
  stderr.sync = true
  error_seen = false
  @tb_stdout_thread = Thread.new(stdout) { |stdout_io|
    begin
      while true
        STDOUT.write(stdout_io.readpartial(1024))
      end
    rescue EOFError
    end
  }
  @tb_stderr_thread = Thread.new(stderr) { |stderr_io|
    begin
      while true
        STDERR.write(stderr_io.readpartial(1024))
        error_seen = true
      end
    rescue EOFError
    end
  }
  start = Time.now
  booted = false
  timeout = 60
  while (Time.now - start) < timeout do
    uri = URI.parse("#{Capybara.app_host}#{context}")
    begin
      response = Net::HTTP.get_response(uri)
      booted = true
      break
    rescue Exception
      sleep 0.2 # sleep and retry
    end
    break if error_seen
  end
  if !booted
    raise "Application #{app_dir} failed to start within #{timeout} seconds"
  end
end

def __torquebox_stop
  if @tb_pid
    begin
      Process.kill 'INT', @tb_pid
    rescue Errno::ESRCH
      # ignore no such process errors - it died already
    end
    @tb_pid = nil
    @tb_stdout_thread.join
    @tb_stdout_thread = nil
    @tb_stderr_thread.join
    @tb_stderr_thread = nil
  end
end
