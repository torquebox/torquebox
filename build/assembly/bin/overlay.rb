#!/usr/bin/env ruby
#
# Overlays TorqueBox on top of another *box (Immutant, for example).
# ARGV[0] should be the path to the root of the distribution. Example:
#
# ./bin/overlay.rb ~/work/immutant/build/assembly/target/stage/immutant 

$: << File.dirname( __FILE__ )

require 'assemble'
require 'fileutils'

class Overlayer
  def initialize(base_dir)
    @assembler = Assembler.new
    @assembler.tool.torquebox_dir = base_dir
    @assembler.tool.jboss_dir = base_dir + '/jboss'
    @assembler.tool.jruby_dir = base_dir + '/jruby'
    
    @config_stash = base_dir + '/../../torquebox_config_stash'
    FileUtils.mkdir_p( @config_stash )
    @assembler.config_stash = @config_stash
  end

  def overlay
    @assembler.lay_down_jruby
    @assembler.install_modules
    @assembler.install_gems
    @assembler.install_share
    @assembler.transform_configs
    @assembler.transform_host_config
  end
end

if __FILE__ == $0 || '-e' == $0 # -e == called from mvn
  Overlayer.new( ARGV[0] ).overlay
end
