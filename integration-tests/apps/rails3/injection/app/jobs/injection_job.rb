class InjectionJob
  include TorqueBox::Injectors

  def run
    queue = inject('queue/injection_job')
    queue.publish('it worked')
  end
end
