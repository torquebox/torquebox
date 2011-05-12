# Copyright 2011 Red Hat, Inc, and individual contributors.
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


# Helpers for upstart rake tasks

require 'torquebox/deploy_utils'

module TorqueBox
  module Upstart
    class << self

      def sys_root
        '/'
      end

      def opt_dir
        File.join( sys_root, 'opt' )
      end

      def opt_torquebox
        File.join( opt_dir, 'torquebox' )
      end

      def init_dir
        File.join( sys_root, 'etc', 'init' )
      end

      def init_script
        File.join( TorqueBox::DeployUtils.torquebox_home, 'share', 'init', 'torquebox.conf' )
      end

      def init_torquebox
        File.join( init_dir, 'torquebox.conf' )
      end

      def create_symlink
        unless File.exist? opt_dir
          if !File.writable?( sys_root )
            puts "Cannot write to #{sys_root}. Please ensure #{opt_torquebox} points to your torquebox installation."
          else
            puts "Creating #{opt_dir}"
            Dir.new( opt_dir )
          end
        end

        unless File.exist?( opt_torquebox )
          if File.writable?( opt_dir )
            puts "Linking #{opt_torquebox} to #{TorqueBox::DeployUtils.torquebox_home}"
            File.symlink( TorqueBox::DeployUtils.torquebox_home, opt_torquebox )
          else
            puts "Cannot write to #{opt_dir}. Upstart expects #{opt_torquebox} to point to #{TorqueBox::DeployUtils.torquebox_home}"
          end
        end
      end

      def copy_init_script
        if File.writable?( init_dir )
          FileUtils.cp( init_script, init_dir )
        else
          puts "Cannot write upstart configuration to #{init_dir}. You'll need to copy #{init_script} to #{init_dir} yourself."
        end
      end

      def check_install
        raise "TorqueBox not installed in #{opt_torquebox}" unless ( File.exist?( opt_torquebox ) )
        puts "TorqueBox install OK: #{opt_torquebox}"
        raise "#{init_torquebox} not installed in #{init_dir}" unless ( File.exist?( init_torquebox ) )
        puts "TorqueBox init scripts OK: #{init_torquebox}"
      end

    end
  end
end

