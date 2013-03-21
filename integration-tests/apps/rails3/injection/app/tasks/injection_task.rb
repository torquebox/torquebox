class InjectionTask < TorqueBox::Messaging::Task

  def publish_message(payload)
    queue = TorqueBox.fetch('/queues/injection_task')
    queue.publish('it worked')
    nil
  end
end
