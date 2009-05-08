
namespace :jboss do
  namespace :rails do

    desc "Check for vendor/rails"
    task :check_vendorized do
     vendor_rails = File.exist?("#{RAILS_ROOT}/vendor/rails")
     raise "Rails must be frozen to run from JBoss.  Try 'rake rails:freeze:gems'" unless vendor_rails
    end

    desc "Deploy the Rails app"
    task :deploy=>['jboss:as:check', 'jboss:rails:check_vendorized'] do
      app_name = File.basename( RAILS_ROOT )
      JBoss::RakeUtils.deploy( app_name, RAILS_ROOT )
      puts "Deployed #{app_name}"
    end

    desc "Undeploy the Rails app"
    task :undeploy=>['jboss:as:check'] do
      app_name = File.basename( RAILS_ROOT )
      JBoss::RakeUtils.undeploy( app_name )
      puts "Undeployed #{app_name}"
    end
  end
end
