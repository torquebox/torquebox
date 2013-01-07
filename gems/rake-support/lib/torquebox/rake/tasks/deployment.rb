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

namespace :torquebox do

  desc "Deploy the app in the current directory"
  task :deploy, [:context_path, :name] => ['torquebox:check'] do |t, args|
    descriptor = TorqueBox::DeployUtils.basic_deployment_descriptor( :context_path => args[:context_path] )
    deployment_name, deploy_dir = TorqueBox::DeployUtils.deploy_yaml( descriptor, args )
  
    puts "Deployed: #{deployment_name}"
    puts "    into: #{deploy_dir}"
  end

  desc "Undeploy the app in the current directory"
  task :undeploy, [:name] => ['torquebox:check'] do |t, args|
    deploy_name, deploy_dir = TorqueBox::DeployUtils.undeploy_yaml( args ) # try -knob.yml first
    unless deploy_name
      deploy_name, deploy_dir = TorqueBox::DeployUtils.undeploy_archive( args )
    end

    if deploy_name
      puts "Undeployed: #{deploy_name}"
      puts "      from: #{deploy_dir}"
    else
      puts "Nothing to undeploy"
    end
  end

  desc "Create (if needed) and deploy as application archive"
  namespace :deploy do
    task :archive, [:name] => [ 'torquebox:archive' ] do |t, args|
      archive_name, deploy_dir = TorqueBox::DeployUtils.deploy_archive( args )
      
      puts "Deployed: #{archive_name}"
      puts "    into: #{deploy_dir}"
    end
  end
  
  desc "Undeploy an application archive"
  namespace :undeploy do
    task :archive, [:name] => [ 'torquebox:check' ] do |t, args|
      deploy_name, deploy_dir = TorqueBox::DeployUtils.undeploy_archive( args )
      
      if deploy_name
        puts "Undeployed: #{deploy_name}"
        puts "      from: #{deploy_dir}" 
      else
        puts "Nothing to undeploy"
      end
    end
  end
   
end

