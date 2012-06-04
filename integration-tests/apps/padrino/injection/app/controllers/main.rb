Myapp.controllers do
  include TorqueBox::Injectors

  get '/from-controller' do
    service = fetch('service:ControllerService')
    queue = fetch('/queue/controller')
    render_injections(service, queue)
  end
end
