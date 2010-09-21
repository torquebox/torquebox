
namespace :torquebox do
  desc "Check your installation of the TorqueBox server"
  task :check do
    matching = Dir[ "#{JBoss::RakeUtils.deployers_dir}/torquebox*deployer*" ] 
    raise "No TorqueBox deployer installed in #{JBoss::RakeUtils.deployers_dir}" if ( matching.empty? )
    puts "TorqueBox Server OK: #{JBoss::RakeUtils.server_dir}"
  end

  desc "Run TorqueBox server"
  task :run=>[ :check ] do
    JBoss::RakeUtils.run_server
  end

end
