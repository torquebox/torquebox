class ParentlessQueueConsumer
  include TorqueBox::Injectors

  def process!(message)
    body = message.decode
    puts "process!: #{body}"
    fetch('/queues/results').publish( "#{self.class.name}=#{body}" )
  end
  
end
