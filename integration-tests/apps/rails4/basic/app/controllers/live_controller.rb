class LiveController < ApplicationController
  include ActionController::Live

  def sse
    response.headers['Content-Type'] = 'text/event-stream'
    sse = SSE.new(response.stream)
    sse.write('test1')
    sse.write('test2')
    sse.write('test3')
    sse.write('test4')
  ensure
    sse.close
  end

end
