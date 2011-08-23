class Processor < TorqueBox::Messaging::MessageProcessor

  def on_message(msg)
    puts "JC: on_message"
    Thing.create(:name => msg)
    puts "JC: Thing.created"
    raise msg if msg =~ /error/
  end

end
