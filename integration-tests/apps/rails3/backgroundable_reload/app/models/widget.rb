class Widget < ActiveRecord::Base
  def foo
    @queue = TorqueBox::Messaging::Queue.new('/queues/background')
    @queue.publish('a response')
    puts "published 'a response'"
    sleep(3)
  end
  always_background :foo
end
