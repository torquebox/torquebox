puts 'building /target/build-metadata.json'
require 'java'
require 'rubygems'
require 'json'
require File.join( File.dirname( __FILE__ ), '../../../modules/core/target/torquebox-core.jar' )

props = org.torquebox.core.util.BuildInfo.new
torquebox = props.getComponentInfo( 'TorqueBox' )

metadata = {}
metadata['build_revision'] = torquebox['build.revision']
metadata['build_number'] = torquebox['build.number']
metadata['build_time'] = Time.now.to_i
dist_file = './target/torquebox-dist-bin.zip'
metadata['dist_size'] = File.size( dist_file )
File.open('./target/build-metadata.json', 'w') do |f|
  f.write( metadata.to_json )
end
