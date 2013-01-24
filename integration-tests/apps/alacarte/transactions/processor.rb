#require 'torquebox-cache'
require 'torquebox-messaging'

class Processor < TorqueBox::Messaging::MessageProcessor

  include TorqueBox::Injectors

  def on_message(msg)
    puts "JC: message is #{message.jms_message}"
    output = fetch( '/queue/output' )
    response = 'yay!'

    if msg =~ /\s+(\d)\s+retries/
      response = test_retries($1.to_i)
    end

    if msg =~ /receive from (.*)/
      queue = TorqueBox::Messaging::Queue.new $1
      response = "got " + queue.receive(:timeout => 10_000)
      puts "JC: response=#{response}"
    end

    output.publish(response)

    # Important to raise the error *after* publishing messages because
    # the error should rollback the publishes
    raise msg if msg =~ /error/
  end

  def test_retries(count)
    if $count == count
      $count = 0
      "retried successfully #{count} times"
    else
      $count ||= 0
      $count += 1
      raise "retry count=#{$count}"
    end
  end
end

