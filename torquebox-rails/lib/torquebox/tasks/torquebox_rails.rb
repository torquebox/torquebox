
namespace :torquebox do
  namespace :rails do

    desc "Deploy the Rails app"
    task :deploy, :context_path, :needs =>['torquebox:server:check'] do |t, args|
      args.with_defaults(:context_path => '/')
      app_name = File.basename( RAILS_ROOT )
      JBoss::RakeUtils.deploy( app_name, RAILS_ROOT, args[:context_path] )
      puts "Deployed #{app_name}"
    end

    desc "Undeploy the Rails app"
    task :undeploy=>['torquebox:server:check'] do
      app_name = File.basename( RAILS_ROOT )
      JBoss::RakeUtils.undeploy( app_name )
      puts "Undeployed #{app_name}"
    end
 
    namespace :deploy do
      desc "Deploy the bundled Rails app"
      task :bundle=>[ 'torquebox:rails:bundle' ] do
        bundle_name = File.basename( RAILS_ROOT ) + '.jar'
        src  = "#{RAILS_ROOT}/#{bundle_name}"
        dest = "#{ENV['JBOSS_HOME']}/server/default/deploy/"
        puts "deploying #{bundle_name}"
        FileUtils.cp( src, dest )
      end
    end

    desc "Bundle the Rails app"
    task :bundle do
      puts "Bundling #{RAILS_ROOT}"
      bundle_name = File.basename( RAILS_ROOT ) + '.jar'
      FileUtils.rm_rf( bundle_name )
  
      skip_files = [
        'log',
        'tmp',
      ]
      include_files = []
      Dir[ "#{RAILS_ROOT}/*" ].each do |entry|
        entry = File.basename( entry )
        unless ( skip_files.include?( entry ) )
          include_files << entry
        end
      end
    
      puts "building bundle #{bundle_name}"
    
      Dir.chdir( RAILS_ROOT ) do
        cmd = "jar cf #{bundle_name} #{include_files.join(' ')}"
        Open3.popen3( cmd ) do |stdin, stdout, stderr|
          stdin.close
          stdout_thr = Thread.new(stdout) {|stdout_io|
            stdout_io.readlines.each do |l|
              puts l
            end
            stdout_io.close
          }
          stderr_thr = Thread.new(stderr) {|stderr_io|
            stderr_io.readlines.each do |l|
              puts l
            end
          }
          stdout_thr.join
          stderr_thr.join
        end
      end
    end
  end
end

