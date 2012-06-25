class InjectionJob
  include TorqueBox::Injectors

  def run
    queue = fetch('/queues/injection_job')
    queue.publish('it worked')
  end
end
