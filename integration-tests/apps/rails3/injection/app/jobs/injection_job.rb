class InjectionJob
  include TorqueBox::Injectors

  def run
    queue = inject('/queues/injection_job')
    queue.publish('it worked')
  end
end
