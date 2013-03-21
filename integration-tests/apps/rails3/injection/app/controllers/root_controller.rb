class RootController < ApplicationController

  def service
    queue = TorqueBox.fetch('/queues/injection_service')
    @message = queue.receive(:timeout => 120_000)
    render 'injection.html.erb'
  end

  def job
    queue = TorqueBox.fetch('/queues/injection_job')
    @message = queue.receive(:timeout => 120_000)
    render :injection
  end

  def task
    InjectionTask.async(:publish_message)
    queue = TorqueBox.fetch('/queues/injection_task')
    @message = queue.receive(:timeout => 120_000)
    render 'injection.html.erb'
  end

  def alt_inject
    queue = TorqueBox.fetch('/queues/injection_job')
    @message = queue.receive(:timeout => 120_000)
    render :injection
  end

  def enumerable
    EnumerableThing.new
    queue = TorqueBox.fetch('/queues/injection_enumerable')
    @message = queue.receive(:timeout => 120_000)
    render :injection
  end

  def predetermined
    @service_registry = TorqueBox.fetch( 'service-registry' )
    @service_target   = TorqueBox.fetch( 'service-target' )
    render 'predetermined.html.erb'
  end
end
