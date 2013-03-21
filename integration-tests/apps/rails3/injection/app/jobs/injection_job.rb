class InjectionJob

  def run
    queue = TorqueBox.fetch('/queues/injection_job')
    queue.publish('it worked')
  end
end
