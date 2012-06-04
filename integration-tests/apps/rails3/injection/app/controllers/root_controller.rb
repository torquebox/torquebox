class RootController < ApplicationController
  include TorqueBox::Injectors

  def service
    queue = fetch('/queues/injection_service')
    @message = queue.receive(:timeout => 120_000)
    render 'injection.html.erb'
  end

  def job
    queue = fetch('/queues/injection_job')
    @message = queue.receive(:timeout => 120_000)
    render :injection
  end

  def task
    InjectionTask.async(:publish_message)
    queue = fetch('/queues/injection_task')
    @message = queue.receive(:timeout => 120_000)
    render 'injection.html.erb'
  end

  def alt_inject
    queue = fetch('/queues/injection_job')
    @message = queue.receive(:timeout => 120_000)
    render :injection
  end

  def enumerable
    EnumerableThing.new
    queue = fetch('/queues/injection_enumerable')
    @message = queue.receive(:timeout => 120_000)
    render :injection
  end
  
  def predetermined
    @service_registry = fetch( 'service-registry' )
    @service_target   = fetch( 'service-target' )
    render 'predetermined.html.erb'
  end
end
