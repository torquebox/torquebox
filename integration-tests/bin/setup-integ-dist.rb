
require 'java'
require 'fileutils'

assembly_dir = File.expand_path( ARGV[0] )
output_dir   = File.expand_path( ARGV[1] )

puts "assembly from... #{assembly_dir}"
puts "output to....... #{output_dir}"

Dir.chdir( assembly_dir ) do
  if ( java.lang::System.getProperty( 'os.name' ) =~ /.*windows.*/i )
    puts "*** Syncing to integ-dist by copy"
    if ( File.exists?( output_dir ) )
      puts "*** Only copying torquebox modules"
      modules_path = 'jboss/modules/org/torquebox'
      FileUtils.rm_rf( "#{output_dir}/#{modules_path}" )
      FileUtils.mkdir_p( File.dirname( "#{output_dir}/#{modules_path}" ) )
      FileUtils.cp_r( "./#{modules_path}", "#{output_dir}/#{modules_path}" )
    else
      FileUtils.mkdir_p( File.dirname( output_dir ) )
      FileUtils.rm_rf( output_dir )
      FileUtils.cp_r( '.', output_dir )
    end
  else
    puts "*** Syncing to integ-dist by rsync"
    FileUtils.mkdir_p( File.dirname( output_dir ) )
    cmd = [ 'rsync -av . --relative',
            '--include jboss/modules',
            '--include jboss/standalone',
            '--exclude jruby/share/ri',
            '--exclude jruby/lib/ruby/gems/1.8/doc',
            output_dir ].join( ' ' )
    puts cmd
    puts `#{cmd}`
  end
end

FileUtils.rm_rf( Dir["#{output_dir}/jboss/standalone/log/*"] )
FileUtils.rm_rf( Dir["#{output_dir}/jboss/standalone/deployments/*"] )
