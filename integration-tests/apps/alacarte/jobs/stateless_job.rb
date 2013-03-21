class StatelessJob

  def initialize
    @already_published = false
    @response_queue = TorqueBox.fetch('/queue/stateless_response')
  end

  def run
    @response_queue.publish('done') unless @already_published
    @already_published = true
  end
end
