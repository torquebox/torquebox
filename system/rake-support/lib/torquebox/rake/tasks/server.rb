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

require 'rake'
require 'torquebox/deploy_utils'

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

  desc "Install TorqueBox as an upstart service"
  task :upstart=>[ :check ] do

    opt_dir = "/opt"
    unless File.exist? opt_dir
      if !File.writable?( '/' )
        puts "Cannot write to /. Upstart expects /opt/torquebox to point to your torquebox installation."
      else
        puts "Creating #{opt_dir}"
        Dir.new( opt_dir )
      end
    end

    tb_link = File.join( opt_dir, "torquebox" ) 
    unless File.exist?( tb_link )
      if File.writable?( opt_dir )
        puts "Symlinking #{tb_link} to #{TorqueBox::DeployUtils.torquebox_home}"
        File.symlink( TorqueBox::DeployUtils.torquebox_home, File.join( '/', 'opt', 'torquebox' ) )
      else
        puts "Cannot write to /opt. Upstart expects /opt/torquebox to point to #{TorqueBox::DeployUtils.torquebox_home}"
      end
    end

    init_dir  = "/etc/init"
    conf_file = File.join( TorqueBox::DeployUtils.torquebox_home, 'share', 'torquebox.conf' )
    if File.writable?( init_dir )
      FileUtils.cp( conf_file, init_dir )
    else
      puts "Cannot write upstart configuration to #{init_dir}. You'll need to copy #{conf_file} to #{init_dir} yourself."
    end

    puts "Done!"
    puts "Ensure that you have a 'torquebox' user with ownership or write permissions of /opt/torquebox"
  end

end
