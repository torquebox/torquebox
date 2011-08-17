require 'torquebox-messaging'

class Processor < TorqueBox::Messaging::MessageProcessor

  include TorqueBox::Injectors

  def on_message(msg)
    output = inject( '/queue/output' )
    if msg =~ /\s+(\d)\s+retries/
      output.publish(test_retries($1.to_i))
    else
      output.publish('yay!')
    end
    # Important to raise the error *after* publishing messages because
    # the error should rollback the publishes
    raise msg if msg =~ /error/
  end

  def test_retries(count)
    if $count == count
      "retried successfully #{count} times"
    else
      $count ||= 0
      $count += 1
      raise "retry count=#{$count}"
    end
  end
end

