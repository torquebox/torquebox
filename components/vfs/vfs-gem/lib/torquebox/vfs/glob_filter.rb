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

require 'torquebox/vfs/glob_translator'


module TorqueBox
  module VFS
    class GlobFilter
      include Java::org.jboss.vfs.VirtualFileFilter
      
      def initialize(child_path, glob)
        regexp_str = GlobTranslator.translate( glob )
        if ( child_path && child_path != '' )
          if ( child_path[-1,1] == '/' )
            regexp_str = "^#{child_path}#{regexp_str}$"
          else
            regexp_str = "^#{child_path}/#{regexp_str}$"
          end
        else
          regexp_str = "^#{regexp_str}$"
        end
        @regexp = Regexp.new( regexp_str ) 
      end
      
      def accepts(file)
        !!( file.path_name =~ @regexp )
      end
    end
  end
end

