class ParentlessQueueConsumer

  def process!(message)
    body = message.decode
    puts "process!: #{body}"
    TorqueBox.fetch('/queues/results').publish( "#{self.class.name}=#{body}" )
  end

end
