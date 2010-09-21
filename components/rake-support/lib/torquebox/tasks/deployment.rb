
def rails_deployment_name( app_name )
  "#{app_name}-rails.yml"
end

def rack_deployment_name( app_name )
  "#{app_name}-rack.yml"
end

def rails_deployment(app_name, root, context_path)
  deployment_descriptor = {
    'application' => {
      'RAILS_ROOT'=>root,
      'RAILS_ENV'=>( defined?( RAILS_ENV ) ? RAILS_ENV : 'development' ),
    },
    'web' => {
      'context'=> context_path[0,1] != '/'? %Q(/#{context_path}) : context_path
    }
  }

  [ rails_deployment_name( app_name ), deployment_descriptor ]
end

def rack_deployment(app_name, root, context_path)
  deployment_descriptor = { 
    'application' => {
      'RACK_ROOT'=>root,
      'RACK_ENV'=>( defined?( RACK_ENV ) ? RACK_ENV : 'development' ),
    },
    'web' => {
      'context'=> context_path[0,1] != '/'? %Q(/#{context_path}) : context_path
    }
  }

  [ rack_deployment_name( app_name ), deployment_descriptor ]
end

def rails?(root)
  File.exist?( File.join( root, 'config', 'environment.rb' ) )
end

def rack?(root)
  File.exist?( File.join( root, 'config.ru' ) )
end

def deployment(app_name, root, context_path)
  if ( rails?( root ) )
    return rails_deployment( app_name, root, context_path )
  elsif ( rack?( root ) )
    return rack_deployment( app_name, root, context_path )
  end
end

def deployment_name(app_name, root )
  if ( rails?( root ) )
    return rails_deployment_name( app_name )
  elsif ( rack?( root ) )
    return rack_deployment_name( app_name )
  end
end

namespace :torquebox do

  desc "Deploy the app in the current directory"
  task :deploy, :context_path, :needs =>['torquebox:check'] do |t, args|
    args.with_defaults(:context_path => '/')
    app_name = File.basename( Dir.pwd )
    deployment_name, deployment_descriptor = deployment( app_name, Dir.pwd, args[:context_path] )
    JBoss::RakeUtils.deploy_yaml( deployment_name, deployment_descriptor )
    puts "Deployed #{deployment_name}"
  end

  desc "Undeploy the app in the current directory"
  task :undeploy=>['torquebox:check'] do
    app_name = File.basename( Dir.pwd )
    JBoss::RakeUtils.undeploy( deployment_name( app_name, Dir.pwd ) )
    puts "Undeployed #{deployment_name}"
  end
 
end

