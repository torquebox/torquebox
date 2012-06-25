class InjectionTask < TorqueBox::Messaging::Task
  include TorqueBox::Injectors

  def publish_message(payload)
    queue = fetch('/queues/injection_task')
    queue.publish('it worked')
    nil
  end
end
