
namespace :jboss do
  namespace :as do

    desc "Check your installation of JBoss-Rails"
    task :check do
      matching = Dir[ "#{JBoss::RakeUtils.deployers_dir}/jboss-rails*deployer*" ] 
      raise "No JBoss-Rails deployer installed in #{JBoss::RakeUtils.deployers_dir}" if ( matching.empty? )
      puts "JBoss-Rails server: #{JBoss::RakeUtils.server_dir}"
    end

    desc "Run JBoss-Rails server"
    task :run=>[ :check ] do
      JBoss::RakeUtils.run_server
    end

  end
end
