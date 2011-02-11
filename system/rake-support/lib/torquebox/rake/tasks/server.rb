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
