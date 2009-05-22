
namespace :torquebox do
  namespace :rails do

    desc "Check for that Rails has been frozen"
    task :check_frozen do
     vendor_rails = File.exist?("#{RAILS_ROOT}/vendor/rails")
     raise "Rails must be frozen to run from TorqueBox.  Try 'rake rails:freeze:gems'" unless vendor_rails
    end

    desc "Deploy the Rails app"
    task :deploy=>['torquebox:server:check', 'torquebox:rails:check_frozen'] do
      app_name = File.basename( RAILS_ROOT )
      JBoss::RakeUtils.deploy( app_name, RAILS_ROOT )
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
