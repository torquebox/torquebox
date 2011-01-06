require 'java'
require 'fileutils'

assembly_dir = ARGV[0]
output_dir   = ARGV[1]
root_war     = ARGV[2]

puts "assembly from... #{assembly_dir}"
puts "output to....... #{output_dir}"
puts "ROOT.war from... #{root_war}"

if ( assembly_dir == output_dir )
  puts "*** Setting up integration tests against assembly, NOT A COPY."
else
  Dir.chdir( assembly_dir ) do
    if ( java.lang::System.getProperty( 'os.name' ) =~ /.*windows.*/i )
      puts "*** Syncing to integ-dist by copy"
      FileUtils.rm_rf( output_dir )
      FileUtils.cp_r( '.', output_dir )
    else
      puts "*** Syncing to integ-dist by rsync"
      cmd = [ 'rsync -a . --relative',
	      '--include jboss/server/default',
	      '--exclude "default/deploy/*.yml',
	      '--exclude default/data',
	      '--exclude default/work',
	      '--exclude default/log',
	      '--exclude default/tmp',
	      '--exclude "jboss/server/*',
	      '--exclude jruby/share/ri',
	      '--exclude jruby/lib/ruby/gems/1.8/doc',
	      output_dir ].join( ' ' )
      puts `#{cmd}` 
    end
  end
end

# Necessary for Arquillian...
if ( ! File.exists?( "#{output_dir}/jboss/server/default/deploy/ROOT.war" ) ) 
  puts "*** Installing ROOT.war"
  FileUtils.cp( root_war, "#{output_dir}/jboss/server/default/deploy/" )
end
