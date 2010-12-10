
def get_archive_name root=Dir.pwd
  File.basename(root) + (rails?(root) ? '.rails' : '.rack')
end

namespace :torquebox do

  desc "Create a self-contained application archive"
  task :archive do 
    skip_files = %w{ ^log$ ^tmp$ \.rails$ \.rack$ }

    include_files = []
    Dir[ "*", ".bundle" ].each do |entry|
      entry = File.basename( entry )
      unless ( skip_files.any?{ |regex| entry.match(regex)} )
        include_files << entry
      end
    end

    archive_name = get_archive_name
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

end


