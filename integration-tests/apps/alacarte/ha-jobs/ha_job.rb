require 'torquebox'

class HaJob

  def initialize
    @queue = TorqueBox.fetch('/queues/node_name')
  end

  def run()
    @queue.receive_and_publish(:timeout => 750) do |message|
      java.lang.System.getProperty('jboss.node.name')
    end
  end

end
