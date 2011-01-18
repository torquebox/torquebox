require 'rake'

namespace :torquebox do
  desc "Check your installation of the TorqueBox server"
  task :check do
    matching = Dir[ "#{TorqueBox::RakeUtils.deployers_dir}/torquebox*deployer*" ] 
    raise "No TorqueBox deployer installed in #{TorqueBox::RakeUtils.deployers_dir}" if ( matching.empty? )
    puts "TorqueBox Server OK: #{TorqueBox::RakeUtils.server_dir}"
  end

  desc "Run TorqueBox server"
  task :run=>[ :check ] do
    TorqueBox::RakeUtils.run_server
  end

end
