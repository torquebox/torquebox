class StatelessJob
  include TorqueBox::Injectors

  def initialize
    @already_published = false
    @response_queue = fetch('/queue/stateless_response')
  end

  def run
    @response_queue.publish('done') unless @already_published
    @already_published = true
  end
end
