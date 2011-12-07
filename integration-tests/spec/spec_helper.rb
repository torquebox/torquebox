require 'torquespec'
require 'fileutils'
require 'rbconfig'
require 'torquebox-rake-support'

$: << File.dirname( __FILE__ )

TorqueSpec.local {
  require 'spec_helper_integ'
}

TorqueSpec.configure do |config|
  config.jboss_home = File.expand_path( File.join( File.dirname( __FILE__ ), '..', 'target', 'integ-dist', 'jboss' ) )
  config.max_heap = java.lang::System.getProperty( 'max.heap' )
  config.lazy = java.lang::System.getProperty( 'jboss.lazy' ) == "true"
  config.jvm_args += " -Dgem.path=default"
  #config.jvm_args += " -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=y"
  config.knob_root = File.expand_path( File.join( File.dirname( __FILE__ ), '..', 'target', 'knobs' ) )
  config.spec_dir = File.dirname( __FILE__ )
end
FileUtils.mkdir_p(TorqueSpec.knob_root) unless File.exist?(TorqueSpec.knob_root)

MUTABLE_APP_BASE_PATH  = File.join( File.dirname( __FILE__ ), '..', 'target', 'apps' )
TESTING_ON_WINDOWS = ( java.lang::System.getProperty( "os.name" ) =~ /windows/i )

def mutable_app(path)
  full_path = File.join( MUTABLE_APP_BASE_PATH, path )
  dest_path = File.dirname( full_path )
  FileUtils.rm_rf( full_path )
  FileUtils.mkdir_p( dest_path )
  FileUtils.cp_r( File.join( File.dirname( __FILE__ ), '..', 'apps', path ), dest_path )
end

def jruby_binary
  File.expand_path( File.join( File.dirname( __FILE__ ), '..', 'target', 'integ-dist', 'jruby', 'bin', 'jruby' ) )
end

def integ_jruby(command)
  # We wrap the command so we can clear GEM_HOME and GEM_PATH, thus using
  # the integ-dist gems instead of maven gems
  wrapped_command = "#{jruby_binary} -J-Dgem.path=default -e \"ENV.delete('GEM_HOME'); ENV.delete('GEM_PATH'); puts `#{jruby_binary} #{command} 2>&1`\""
  # We use IO.popen4 instead of backticks because nested backticks didn't
  # play nice
  output = ""
  IO.popen4(wrapped_command) do |pid, stdin, stdout, stderr|
    stdin.close
    stdout_reader = Thread.new(stdout) { |stdout_io|
      stdout_io.each_line { |l| output << l }
      stdout_io.close
    }
    stderr_reader = Thread.new(stderr) { |stderr_io|
      stderr_io.each_line { |l| output << l }
      stderr_io.close
    }
    [stdout_reader, stderr_reader].each(&:join)
    output
  end
end

# Because DRb requires ObjectSpace and 1.9 disables it
require 'jruby'
JRuby.objectspace = true
