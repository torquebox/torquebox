
class MyConsumer
  def process!(msg)
    puts "received: #{msg.text}"
  end
end
