class Widget < ActiveRecord::Base

  def foo(call_count)
    @responseq = TorqueBox.fetch('/queues/response')
    @responseq.publish("response 0")
    puts "published 'response 0'"
    Rewriter.rewrite_file( __FILE__, "response #{call_count - 1}", "response #{call_count}" )
  end
  always_background :foo
end
