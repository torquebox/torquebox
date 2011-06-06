class LoopyService

  attr_accessor :num_loops

  def initialize(opts={})
    @num_loops = 0
  end

  def start()
    @should_run = true
    spawn_thread()
  end

  def spawn_thread()
    @thread = Thread.new do
      loop_once while @should_run
    end
  end

  def loop_once
    @num_loops += 1
  end

  def stop()
    @should_run = false
    @thread.join
  end

end
