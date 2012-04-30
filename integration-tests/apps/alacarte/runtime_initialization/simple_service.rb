
require 'torquebox-messaging'

class SimpleService
  include TorqueBox::Injectors

  def initialize(opts={})
    @queue = inject('/queues/tb_init_test')
    @context_queue = inject('/queues/service_context')
  end

  def start()
    @queue.publish( PART_ONE + PART_TWO )
    @context_queue.publish( ENV['TORQUEBOX_CONTEXT'] )
  end
end

