class Processor < TorqueBox::Messaging::MessageProcessor
  def on_message(msg)
    Thing.create(:name => msg)
    puts "JC: Thing.created count=#{Thing.count}"
    raise msg if msg =~ /error/
  end
end

