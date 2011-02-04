
require 'rake'

def deployment(app_name, root, context_path)
  env = defined?(RACK_ENV) ? RACK_ENV : ENV['RACK_ENV']
  if ( env.nil? ) 
    env = defined?(RAILS_ENV) ? RAILS_ENV : ENV['RAILS_ENV']
  end

  deployment_descriptor = {
    'application' => {
      'root'=>root,
      'env'=>( env || 'development' ).to_s,
    },
    'web' => {
      'context'=> context_path[0,1] != '/'? %Q(/#{context_path}) : context_path
    }
  }

  [ "#{app_name}-knob.yml", deployment_descriptor ]
end

namespace :torquebox do

  desc "Deploy the app in the current directory"
  task :deploy, :context_path, :needs =>['torquebox:check'] do |t, args|
    args.with_defaults(:context_path => '/')
    app_name = File.basename( Dir.pwd )
    deployment_name, deployment_descriptor = deployment( app_name, Dir.pwd, args[:context_path] )
    TorqueBox::RakeUtils.deploy_yaml( deployment_name, deployment_descriptor )
    puts "Deployed #{deployment_name}"
  end

  desc "Undeploy the app in the current directory"
  task :undeploy=>['torquebox:check'] do
    app_name = File.basename( Dir.pwd )
    deployment_name = "#{app_name}-knob.yml"
    TorqueBox::RakeUtils.undeploy( deployment_name )
    puts "Undeployed #{deployment_name}"
  end

  desc "Create (if needed) and deploy as application archive"
  namespace :deploy do
    task :archive=>[ 'torquebox:archive' ] do
      archive_name = get_archive_name
      src = "#{Dir.pwd}/#{archive_name}"
      FileUtils.cp( src, TorqueBox::RakeUtils.deploy_dir )
      puts "Deployed #{archive_name}"
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

