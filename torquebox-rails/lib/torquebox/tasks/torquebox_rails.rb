
namespace :torquebox do
  namespace :rails do

    desc "Deploy the Rails app"
    task :deploy, :context_path, :needs =>['torquebox:server:check'] do |t, args|
      args.with_defaults(:context_path => '/')
      app_name = File.basename( RAILS_ROOT )
      JBoss::RakeUtils.deploy( app_name, RAILS_ROOT, args[:context_path] )
      puts "Deployed #{app_name}"
    end

    desc "Undeploy the Rails app"
    task :undeploy=>['torquebox:server:check'] do
      app_name = File.basename( RAILS_ROOT )
      JBoss::RakeUtils.undeploy( app_name )
      puts "Undeployed #{app_name}"
    end
  end
end

