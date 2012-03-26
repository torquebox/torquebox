
require 'torquebox-messaging'

class SimpleService
  include TorqueBox::Injectors

  def initialize(opts={})
    @queue = inject('/queues/tb_init_test')
  end

  def start()
    @queue.publish( PART_ONE + PART_TWO )
  end
end

