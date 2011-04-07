# Copyright 2008-2011 Red Hat, Inc, and individual contributors.
# 
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
# 
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public
# License along with this software; if not, write to the Free
# Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA, or see the FSF site: http://www.fsf.org.

require 'rbconfig'
require 'rake'

def get_archive_name root=Dir.pwd
  File.basename(root) + '.knob'
end

namespace :torquebox do

  desc "Create a nice self-contained application archive"
  task :archive do
    skip_files = %w{ ^log$ ^tmp$ ^test$ ^spec$ \.knob$ vendor }

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
    IO.popen4( cmd ) do |pid, stdin, stdout, stderr|
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
      jruby << " --1.9" if RUBY_VERSION =~ /^1\.9\./
      exec_cmd( "#{jruby} -S bundle package" )
      exec_cmd( "#{jruby} -S bundle install --local --path vendor/bundle" )
    end
  end

end


def exec_cmd(cmd)
  IO.popen4( cmd ) do |pid, stdin, stdout, stderr|
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
