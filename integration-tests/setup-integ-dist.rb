require 'java'
require 'fileutils'

assembly_dir = File.expand_path( ARGV[0] )
output_dir   = File.expand_path( ARGV[1] )

puts "assembly from... #{assembly_dir}"
puts "output to....... #{output_dir}"

if ( assembly_dir == output_dir )
  puts "*** Setting up integration tests against assembly, NOT A COPY."
else
  Dir.chdir( assembly_dir ) do
    if ( java.lang::System.getProperty( 'os.name' ) =~ /.*windows.*/i )
      puts "*** Syncing to integ-dist by copy"
      if ( File.exists?( output_dir ) )
        puts "*** Only copying deployers into default profile"
        deployer_path = 'jboss/server/default/deployers/torquebox.deployer' 
        FileUtils.rm_rf( "#{output_dir}/#{deployer_path}" )
        FileUtils.cp_r( "./#{deployer_path}", "#{output_dir}/#{deployer_path}" )

        tb_common_path = 'jboss/common/torquebox'
        FileUtils.rm_rf( "#{output_dir}/#{tb_common_path}" )
        FileUtils.cp_r( "./#{tb_common_path}", "#{output_dir}/#{tb_common_path}" )

        jboss_lib_path = 'jboss/common/lib'
        FileUtils.rm( Dir["#{output_dir}/#{jboss_lib_path}/torquebox*"] )

        Dir[ "./#{jboss_lib_path}/torquebox*" ].each do |f|
          FileUtils.cp( f, "#{output_dir}/#{jboss_lib_path}/" )
        end
      else
        FileUtils.rm_rf( output_dir )
        FileUtils.cp_r( '.', output_dir )
      end
    else
      puts "*** Syncing to integ-dist by rsync"
      cmd = [ 'rsync -a . --relative',
              '--include jboss/modules',
              '--include jboss/standalone',
              #"--exclude 'apps/*'",
              #'--exclude default/deploy/*.yml',
              #'--exclude default/deploy/*.knob',
              #'--exclude default/data',
              #'--exclude default/work',
              #'--exclude default/log',
              #'--exclude default/tmp',
              #'--exclude jboss/server/*',
              #'--exclude jruby/share/ri',
              #'--exclude jruby/lib/ruby/gems/1.8/doc',
              output_dir ].join( ' ' )
      puts cmd
      puts `#{cmd}` 
    end
  end
end

FileUtils.rm_rf( Dir["#{output_dir}/jboss/standalone/log/*"] )

