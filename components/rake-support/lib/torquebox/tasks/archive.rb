if ( rails?( Dir.pwd ) )
  namespace :torquebox do

    desc "Create a self-contained application archive"
    task :archive do 
      puts "Creating archive of #{RAILS_ROOT}"
      bundle_name = File.basename( RAILS_ROOT ) + '.rails'
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
    
      puts "Creating archive: #{bundle_name}"
    
      Dir.chdir( RAILS_ROOT ) do
        unless File.exist? "config/rails-env.yml"
          File.open( "config/rails-env.yml", "w" ) do |out|
            YAML.dump( { "RAILS_ENV" => "production" }, out )
          end
        end
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
      puts "Created archive: #{bundle_name}"
    end
  end

end

