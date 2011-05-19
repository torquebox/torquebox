class InjectionTask < TorqueBox::Messaging::Task
  include TorqueBox::Injectors

  def publish_message(payload)
    queue = inject('queue/injection_task')
    queue.publish('it worked')
  end
end
