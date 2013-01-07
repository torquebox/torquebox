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

require 'rake'
require 'torquebox/deploy_utils'
require 'torquebox/upstart'
require 'torquebox/launchd'


namespace :torquebox do
  desc "Check your installation of the TorqueBox server"
  task :check do
    #this will raise if things aren't correct
    TorqueBox::DeployUtils.check_server 
    puts "TorqueBox Server OK: #{TorqueBox::DeployUtils.server_dir}"
  end

  desc "Run TorqueBox server"
  task :run=>[ :check ] do
    TorqueBox::DeployUtils.run_server
  end

  namespace :launchd do
    
    desc "Check if TorqueBox is installed as a launchd daemon"
    task :check=>[ 'torquebox:check' ] do
      TorqueBox::Launchd.check_install
    end

    desc "Install TorqueBox as an launchd daemon"
    task :install=>[ 'torquebox:check' ] do
      TorqueBox::Launchd.install
    end

    desc "Start TorqueBox when running as a launchd daemon"
    task :start=>[ :check ] do
      TorqueBox::DeployUtils.run_command( 'launchctl start org.torquebox.TorqueBox' )
    end

    desc "Stop TorqueBox when running as an launchd daemon"
    task :stop=>[ :check ] do
      TorqueBox::DeployUtils.run_command( 'launchctl stop org.torquebox.TorqueBox' )
    end

    desc "Restart TorqueBox when running as an launchd daemon"
    task :restart=>[ :check ] do
      TorqueBox::DeployUtils.run_command( 'launchctl stop org.torquebox.TorqueBox' )
      TorqueBox::DeployUtils.run_command( 'launchctl start org.torquebox.TorqueBox' )
    end

  end

  namespace :upstart do
    desc "Check if TorqueBox is installed as an upstart service"
    task :check=>[ 'torquebox:check' ] do
      TorqueBox::Upstart.check_install
      puts "TorqueBox is installed as an upstart service at #{TorqueBox::DeployUtils.opt_torquebox}"
    end

    desc "Install TorqueBox as an upstart service"
    task :install=>[ 'torquebox:check' ] do
      TorqueBox::DeployUtils.create_symlink
      TorqueBox::Upstart.copy_init_script
      puts "Done! Ensure that you have a 'torquebox' user with ownership or write permissions of /opt/torquebox"
    end

    desc "Start TorqueBox when running as an upstart service"
    task :start=>[ :check ] do
      TorqueBox::DeployUtils.run_command( 'start torquebox' )
    end

    desc "Stop TorqueBox when running as an upstart service"
    task :stop=>[ :check ] do
      TorqueBox::DeployUtils.run_command( 'stop torquebox' )
    end

    desc "Restart TorqueBox when running as an upstart service"
    task :restart=>[ :check ] do
      TorqueBox::DeployUtils.run_command( 'restart torquebox' )
    end

  end
end



