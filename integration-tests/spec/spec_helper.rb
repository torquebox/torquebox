require 'torquespec'
require 'fileutils'
require 'rbconfig'
require 'torquebox-rake-support'

$: << File.dirname( __FILE__ )

jboss_home = File.expand_path( File.join( File.dirname( __FILE__ ), '..', 'target', 'integ-dist', 'jboss' ) )
jboss_log_dir = File.join( jboss_home, 'standalone', 'log' )

TorqueSpec.local {
  require 'spec_helper_integ'
}

TorqueSpec.configure do |config|
  config.jboss_home = jboss_home
  config.jvm_args = "-Xms256m -Xmx1024m -XX:MaxPermSize=512m -XX:NewRatio=8 -XX:+UseParallelGC -XX:+UseParallelOldGC -XX:SoftRefLRUPolicyMSPerMB=100 -Djruby.home=#{config.jruby_home} -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:#{File.join( jboss_log_dir, 'gc.log' )}"
  config.max_heap = java.lang::System.getProperty( 'max.heap' )
  config.lazy = java.lang::System.getProperty( 'jboss.lazy' ) == "true"
  config.jvm_args += " -Dgem.path=default"
  #config.jvm_args += " -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=y"
  config.knob_root = File.expand_path( File.join( File.dirname( __FILE__ ), '..', 'target', 'knobs' ) )
  config.spec_dir = File.dirname( __FILE__ )

  if java.lang::System.getProperty( "integ.debug" ) == "true"
    config.jvm_args += " -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=y"
  end
end
FileUtils.mkdir_p(TorqueSpec.knob_root) unless File.exist?(TorqueSpec.knob_root)
FileUtils.mkdir_p( jboss_log_dir ) unless File.exist?( jboss_log_dir )

MUTABLE_APP_BASE_PATH  = File.join( File.dirname( __FILE__ ), '..', 'target', 'apps' )
TESTING_ON_WINDOWS = ( java.lang::System.getProperty( "os.name" ) =~ /windows/i )

module TorqueSpec
  module AS7
    def start_command
      ENV['APPEND_JAVA_OPTS'] = "#{TorqueSpec.jvm_args} -Djboss.home.dir=\"#{TorqueSpec.jboss_home}\""
      boot_script = TESTING_ON_WINDOWS ? "standalone.bat" : "standalone.sh"
      "\"#{TorqueSpec.jboss_home}/bin/#{boot_script}\""
    end
  end
end

def mutable_app(path)
  full_path = File.join( MUTABLE_APP_BASE_PATH, path )
  dest_path = File.dirname( full_path )
  FileUtils.rm_rf( full_path )
  FileUtils.mkdir_p( dest_path )
  FileUtils.cp_r( File.join( File.dirname( __FILE__ ), '..', 'apps', path ), dest_path )
end

def jruby_binary
  bin = File.expand_path( File.join( File.dirname( __FILE__ ), '..', 'target', 'integ-dist', 'jruby', 'bin', 'jruby' ) )
  bin << ".exe" if TESTING_ON_WINDOWS
  bin
end

def integ_jruby_launcher
  File.expand_path( File.join( File.dirname( __FILE__ ), 'integ_jruby_launcher.rb' ) )
end

def integ_jruby(command)
  `#{jruby_binary} #{integ_jruby_launcher} "#{command}"`
end

def normalize_path(path)
    path = path.slice(0..0).downcase + path.slice(1..-1) if TESTING_ON_WINDOWS
    File.expand_path(path.strip)
end

def assert_paths_are_equal(actual, expected) 
  normalize_path(actual).should eql(normalize_path(expected))
end

def domain_host_for(server)
  'localhost'
end

def domain_port_for(server, base_port)
  port_offset = 100
  server == :server1 ? base_port : base_port + port_offset
end

# JRuby 1.6.7.2 in 1.9 mode has a bug where it needs ObjectSpace for
# DRb to work. So, we swipe JRuby master's implementation and patch
# things up.
if RUBY_VERSION > '1.9' && JRUBY_VERSION < '1.7'
  require 'drb'
  require 'weakref'
  module DRb
    class DRbIdConv
      # Convert an object reference id to an object.
      #
      # This implementation looks up the reference id in the local object
      # space and returns the object it refers to.
      def to_obj(ref)
        _get(ref)
      end

      # Convert an object into a reference id.
      #
      # This implementation returns the object's __id__ in the local
      # object space.
      def to_id(obj)
        obj.nil? ? nil : _put(obj)
      end

      def _clean
        dead = []
        id2ref.each {|id,weakref| dead << id unless weakref.weakref_alive?}
        dead.each {|id| id2ref.delete(id)}
      end

      def _put(obj)
        _clean
        id2ref[obj.__id__] = WeakRef.new(obj)
        obj.__id__
      end

      def _get(id)
        weakref = id2ref[id]
        if weakref
          result = weakref.__getobj__ rescue nil
          if result
            return result
          else
            id2ref.delete id
          end
        end
        nil
      end

      def id2ref
        @id2ref ||= {}
      end
      private :_clean, :_put, :_get, :id2ref
    end
  end
end
