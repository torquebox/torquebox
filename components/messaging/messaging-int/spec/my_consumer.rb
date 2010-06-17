
class MyConsumer
  def process!(msg)
    puts "processing #{msg.inspect}"
  end
end
