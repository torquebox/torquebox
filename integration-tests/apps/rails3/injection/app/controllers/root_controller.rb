class RootController < ApplicationController
  include TorqueBox::Injectors

  def service
    queue = inject('/queues/injection_service')
    @message = queue.receive(:timeout => 60000)
  end
end
