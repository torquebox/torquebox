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

  namespace :auth do
    desc "Add a username and password to the torquebox-auth security realm with CREDENTIALS=user:pass"
    task :adduser do
      puts "Provide credentials as rake torquebox:auth:adduser CREDENTIALS=user:pass" and return unless ENV['CREDENTIALS']
      credentials = ENV['CREDENTIALS'].gsub(/:/,'=')
      properties_file = "#{TorqueBox::RakeUtils.properties_dir}/torquebox-users.properties"
      File.open( properties_file, 'a' ) do |file|
        file.puts "#{credentials}"
      end
      puts "Credentials written to #{properties_file}"
    end
  end
end

