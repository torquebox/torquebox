require 'java'
require 'torquebox'

class NodeNameService

  def initialize(options={})
    @queue = TorqueBox.fetch('/queues/node_name')
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
