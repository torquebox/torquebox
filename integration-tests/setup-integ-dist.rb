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
  puts "*** Syncing to integ-dist with hardlinks"
  Dir.chdir( assembly_dir ) do
    if ( true || java.lang::System.getProperty( 'os.name' ) =~ /.*windows.*/i )
      puts "need to cp -R on windows, not-yet-implemented."
      FileUtils.cp_r( '.', output_dir )
    else
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
