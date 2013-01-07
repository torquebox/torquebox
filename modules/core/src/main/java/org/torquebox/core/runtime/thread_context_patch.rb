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

class Thread
  class << self
    alias_method :start_before_torquebox, :start
    
    # As TorqueBox does some thread-local book-keeping to maintain
    # knowledge about the current Ruby, etc, and we freely cross
    # between Java to Ruby and back, allowing Ruby code to spawn
    # threads, we hereby patch #start() in order to inherit
    # our thread-local book-keeping to threads created within
    # Ruby code.
    #
    # Specifically, this is needed for drb when running in-container
    # tests, and for the wider case of ruby services spinning their
    # own long-lived thread to drive a loop.
    def start(*args, &block)
      parent_bundle = org.torquebox.core.runtime::ThreadManager.current_bundle
      start_before_torquebox( *args ) do
        org.torquebox.core.runtime::ThreadManager.prepare_thread( parent_bundle )
        begin
          block.call( *args )
        rescue Exception=>e
          puts e.message
        ensure
          org.torquebox.core.runtime::ThreadManager.unprepare_thread( parent_bundle )
        end
      end
    end
  end
end
