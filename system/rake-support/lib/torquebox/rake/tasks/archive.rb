
require 'rbconfig'
require 'rake'

def get_archive_name root=Dir.pwd
  File.basename(root) + '.knob'
end

namespace :torquebox do

  desc "Create a nice self-contained application archive"
  task :archive do
    skip_files = %w{ ^log$ ^tmp$ \.knob$ vendor }

    include_files = []
    Dir[ "*", ".bundle" ].each do |entry|
      entry = File.basename( entry )
      unless ( skip_files.any?{ |regex| entry.match(regex)} )
        include_files << entry
      end
    end

    Dir[ 'vendor/*' ].each do |entry|
      include_files << entry unless ( entry == 'vendor/cache' )
    end

    app_name = File.basename( Dir.pwd )
    archive_name = "#{app_name}.knob"
    puts "Creating archive: #{archive_name}"
    cmd = "jar cvf #{archive_name} #{include_files.join(' ')}"
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
    puts "Created archive: #{Dir.pwd}/#{archive_name}"
  end

  if ( File.exist?( 'Gemfile' ) )
    desc "Freeze application gems"
    task :freeze do
      jruby = File.join( RbConfig::CONFIG['bindir'], RbConfig::CONFIG['ruby_install_name'] )
      exec_cmd( "#{jruby} -S bundle package" )
      exec_cmd( "#{jruby} -S bundle install --local --path vendor/bundle" )
    end
  end

end


def exec_cmd(cmd)
  Open3.popen3( cmd ) do |stdin, stdout, stderr|
    stdin.close
    stdout_thr = Thread.new(stdout) {|stdout_io|
      stdout_io.each_line do |l|
        STDOUT.puts l
        STDOUT.flush
      end
      stdout_io.close
    }
    stderr_thr = Thread.new(stderr) {|stderr_io|
      stderr_io.each_line do |l|
        STDERR.puts l
        STDERR.flush
      end
    }
    stdout_thr.join
    stderr_thr.join
  end
end
