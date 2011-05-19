class RootController < ApplicationController
  include TorqueBox::Injectors

  def service
    queue = inject('queue/injection_service')
    @message = queue.receive(:timeout => 60000)
    render 'injection.html.erb'
  end

  def job
    queue = inject('queue/injection_job')
    @message = queue.receive(:timeout => 60000)
    render :injection
  end

  def task
    InjectionTask.async(:publish_message)
    queue = inject('queue/injection_task')
    @message = queue.receive(:timeout => 60000)
    render 'injection.html.erb'
  end
end
