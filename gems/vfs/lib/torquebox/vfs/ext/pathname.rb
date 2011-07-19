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

require 'pathname'

class Pathname

  alias_method :realpath_without_vfs, :realpath
  alias_method :relative_before_vfs, :relative?

  def realpath
    vfs_path? ? expand_path : realpath_without_vfs
  end

  def vfs_path?
    @path.to_s =~ /^vfs:/
  end

  def relative?
    vfs_path? ? false : relative_before_vfs
  end
end
