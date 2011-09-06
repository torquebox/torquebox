class Processor < TorqueBox::Messaging::MessageProcessor
  def on_message(msg)
    TorqueBox.transaction do
      Thing.create(:name => msg)
      puts "JC: Thing.created count=#{Thing.count}"
    end
    raise msg if msg =~ /error/
  end
end

