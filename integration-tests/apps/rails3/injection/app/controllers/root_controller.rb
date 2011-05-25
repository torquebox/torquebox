class RootController < ApplicationController
  include TorqueBox::Injectors

  def service
    queue = inject('/queues/injection_service')
    @message = queue.receive(:timeout => 60000)
    render 'injection.html.erb'
  end

  def job
    queue = inject('/queues/injection_job')
    @message = queue.receive(:timeout => 60000)
    render :injection
  end

  def task
    InjectionTask.async(:publish_message)
    queue = inject('/queues/injection_task')
    @message = queue.receive(:timeout => 60000)
    render 'injection.html.erb'
  end


  def predetermined
    @service_registry = inject( 'service-registry' )
    @service_target   = inject( 'service-target' )
    render 'predetermined.html.erb'
  end
end
