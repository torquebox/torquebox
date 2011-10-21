


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
          org.torquebox.core.runtime::ThreadManager.unprepare_thread()
        end
      end
    end
  end
end