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
require 'torquebox/rake/tasks/rake_utils'

def deployment_descriptor(root, env, context_path)
  d = {}
  d['application'] = {}
  d['application']['root'] = root
  unless ( env.nil? )
    d['application']['env'] = env.to_s
  end

  if ( context_path.nil? )
    if ( ! ( File.exists?( File.join( root, "torquebox.yml" ) ) || File.exists?( File.join( root, "config", "torquebox.yml" ) ) ) )
      context_path = '/'
    end
  end

  unless ( context_path.nil? )
    d['web'] = {}
    d['web']['context'] = context_path
  end 

  d

end

def deployment(app_name, root, context_path)
  env = defined?(RACK_ENV) ? RACK_ENV : ENV['RACK_ENV']
  if ( env.nil? ) 
    env = defined?(::Rails) ? ::Rails.env : ENV['RAILS_ENV']
  end

  [ "#{app_name}-knob.yml", deployment_descriptor( root, env, context_path) ]
end

namespace :torquebox do

  desc "Deploy the app in the current directory"
  task :deploy, :context_path, :needs =>['torquebox:check'] do |t, args|
    app_name = File.basename( Dir.pwd )
    deployment_name, deployment_descriptor = deployment( app_name, Dir.pwd, args[:context_path] )
    TorqueBox::RakeUtils.deploy_yaml( deployment_name, deployment_descriptor )
    puts "Deployed: #{deployment_name}"
    puts "    into: #{TorqueBox::RakeUtils.deploy_dir}"
  end

  desc "Undeploy the app in the current directory"
  task :undeploy=>['torquebox:check'] do
    app_name = File.basename( Dir.pwd )
    deployment_name = "#{app_name}-knob.yml"
    TorqueBox::RakeUtils.undeploy( deployment_name )
    puts "Undeployed: #{deployment_name}"
  end

  desc "Create (if needed) and deploy as application archive"
  namespace :deploy do
    task :archive=>[ 'torquebox:archive' ] do
      archive_name = get_archive_name
      src = File.join("#{Dir.pwd}", "#{archive_name}")
      FileUtils.cp( src, TorqueBox::RakeUtils.deploy_dir )
      puts "Deployed: #{archive_name}"
      puts "    into: #{TorqueBox::RakeUtils.deploy_dir}"
    end
  end
  namespace :undeploy do
    task :archive do
      archive_name = get_archive_name
      FileUtils.rm_f( File.join( TorqueBox::RakeUtils.deploy_dir, archive_name ) )
      puts "Undeployed #{archive_name}"
    end
  end
 
end

