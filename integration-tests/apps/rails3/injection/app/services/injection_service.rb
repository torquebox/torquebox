class InjectionService
  include TorqueBox::Injectors

  def start
    queue = inject('queue/injection_service')
    queue.publish('it worked')
  end
end
