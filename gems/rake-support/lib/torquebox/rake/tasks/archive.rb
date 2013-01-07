# Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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
require 'torquebox/deploy_utils'

namespace :torquebox do

  desc "Create a nice self-contained application archive"
  task :archive, :name do |t, args|
    path = TorqueBox::DeployUtils.create_archive( args )
    puts "Created archive: #{path}"
  end

  if ( File.exist?( 'Gemfile' ) )
    desc "Freeze application gems"
    task :freeze do
      TorqueBox::DeployUtils.freeze_gems
    end
  end

end



