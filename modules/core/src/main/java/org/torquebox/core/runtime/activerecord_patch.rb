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

class Class
  
  alias_method :method_added_activerecord_vfs, :method_added
  
  def method_added(method_name)
    recursing = Thread.current[:added_ar_vfs]
    unless recursing
      Thread.current[:added_ar_vfs] = true
      # We monkey patch activerecord-jdbc-adapter here to handle sqlite vfs paths. This patch works for
      # activerecord-jdbc-adapter v1.1.3 and up. For lower versions, see vfs/lib/torquebox/vfs/ext/jdbc.rb. 
      if self.to_s == 'ActiveRecord::ConnectionAdapters::JdbcDriver' && method_name == :connection
        self.class_eval do
          alias_method :connection_before_torquebox, :connection

          def connection(url, user, pass)
            connection_before_torquebox( url.sub(':vfs:', ':'), user, pass )
          end
        end
      end
      method_added_activerecord_vfs(method_name)
      Thread.current[:added_ar_vfs] = false
    end
  end # method_added
  
end
