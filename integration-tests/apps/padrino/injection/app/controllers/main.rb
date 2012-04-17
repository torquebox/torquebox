Myapp.controllers do
  include TorqueBox::Injectors

  get '/from-controller' do
    service = inject('service:ControllerService')
    queue = inject('/queue/controller')
    render_injections(service, queue)
  end
end
