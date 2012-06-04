class InjectionService
  include TorqueBox::Injectors

  def start
    queue = fetch('/queues/injection_service')
    queue.publish('it worked')
  end
end
