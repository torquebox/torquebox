require 'torquespec'
require 'fileutils'
require 'torquebox-rake-support'
TorqueSpec.local {
  require 'spec_helper_integ'
}

TorqueSpec.jboss_home = File.expand_path( File.join( File.dirname( __FILE__ ), '..', 'target', 'integ-dist', 'jboss' ) )
TorqueSpec.max_heap = java.lang::System.getProperty( 'max.heap' )
TorqueSpec.lazy = java.lang::System.getProperty( 'jboss.lazy' ) == "true"
TorqueSpec.jvm_args += " -Djruby.home=#{java.lang::System.getProperty( 'jruby.home' )}"

TorqueSpec.knob_root = File.expand_path( File.join( File.dirname( __FILE__ ), '..', 'target', 'knobs' ) )
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

