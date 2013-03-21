class InjectionService

  def start
    queue = TorqueBox.fetch('/queues/injection_service')
    queue.publish('it worked')
  end
end
