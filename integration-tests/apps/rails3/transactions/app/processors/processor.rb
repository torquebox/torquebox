class Processor < TorqueBox::Messaging::MessageProcessor
  def on_message(msg)
    begin
      Thing.create(:name => msg)
      puts "JC: Thing.created count=#{Thing.count}"
    rescue Exception => e
      puts "JC: exception(#{e.class})", $!, $@
      raise
    end
    raise msg if msg =~ /error/
  end
end

