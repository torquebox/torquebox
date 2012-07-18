require 'java'
require 'torquebox'

class NodeNameService
  include TorqueBox::Injectors

  def initialize(options={})
    @queue = inject('/queues/node_name')
    @done = false
  end

  def start
    @thread = Thread.new do
      loop_once until @done
    end
  end

  def loop_once
    @queue.receive_and_publish(:timeout => 500) do |message|
      java.lang.System.getProperty('jboss.node.name')
    end
  end

  def stop
    @done = true
    @thread.join
  end

end
