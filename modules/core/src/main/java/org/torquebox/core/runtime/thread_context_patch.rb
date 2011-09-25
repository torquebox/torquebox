

class Thread
  class << self
    alias_method :start_before_torquebox, :start
    
    def start(*args, &block)
      puts "patched Thread.start"
      parent_bundle = org.torquebox.core.runtime::ThreadManager.current_bundle
      puts "parent #{parent_bundle}"
      start_before_torquebox( *args ) do
        puts "prepare with #{parent_bundle}"
        org.torquebox.core.runtime::ThreadManager.prepare_thread( parent_bundle )
        begin
          puts "call into original block"
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