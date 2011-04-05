class Service
  include TorqueBox::Injectors

  def start
    queue = inject('/queues/injection_service')
    queue.publish('it worked')
  end
end
