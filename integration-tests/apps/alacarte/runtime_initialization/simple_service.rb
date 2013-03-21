
require 'torquebox-messaging'

class SimpleService

  def initialize(opts={})
    @queue = TorqueBox.fetch('/queues/tb_init_test')
    @context_queue = TorqueBox.fetch('/queues/service_context')
  end

  def start()
    @queue.publish( PART_ONE + PART_TWO )
    @context_queue.publish( ENV['TORQUEBOX_CONTEXT'] )
  end
end

