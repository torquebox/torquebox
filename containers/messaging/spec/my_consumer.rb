
class MyConsumer
  def process!(msg)
    puts "processing #{msg.text}"
  end
end
