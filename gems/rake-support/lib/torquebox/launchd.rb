# Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

# Helpers for launchd rake tasks

require 'torquebox/deploy_utils'

module TorqueBox
  module Launchd
    class << self

      def log_dir
        File.join( TorqueBox::DeployUtils.jboss_home, 'standalone', 'logs' )
      end

      def plist_template
        File.join( plist_dir, 'TorqueBoxAgent.plist.template' )
      end
      
      def plist_file
        File.join( plist_dir, 'TorqueBoxAgent.plist' )
      end

      def plist_dir
        File.join( TorqueBox::DeployUtils.torquebox_home, 'share', 'init' )
      end

      def check_install
        raise "#{plist_file} not installed in #{plist_dir}" unless ( File.exist?( plist_file ) )
        puts "TorqueBox plist scripts OK: #{plist_file}."
        
        launchctl_found = false; IO.popen( 'launchctl list | grep torquebox' ) do |output|
          output.each do |line|
            if line =~ /torquebox/
              puts "TorqueBox launchd script OK: #{line}"
              launchctl_found = true
              break
            end
          end
        end
        
        raise "TorqueBox launchd script not found in launchctl." unless launchctl_found
        
      end
      
      def install
        unless File.writable?( plist_dir )
          raise "Cannot write launchd configuration to #{plist_dir}. You'll need to copy #{plist_file} to #{plist_dir} yourself."
        end
        
        File.delete( plist_file ) if File.exists? plist_file 
        lines = File.open( plist_template, 'r' ) { |f| f.readlines }  
        File.open( plist_file, 'w' ) do |file|
          lines.each do |line|
            if line =~ /\$\{TORQUEBOX_HOME\}/
              file.puts( line.sub( /\$\{TORQUEBOX_HOME\}/, TorqueBox::DeployUtils.torquebox_home ) )
            else
              file.puts line
            end
          end
        end
        puts "Created launchd plist #{plist_file}, loading now."
        TorqueBox::DeployUtils.run_command "launchctl load #{plist_file}"
        check_install
        FileUtils.mkdir_p log_dir, :mode => 0755 unless File.exists? log_dir
      end
    
    end
  end
end

