Myapp.controllers do

  get '/from-controller' do
    service = TorqueBox.fetch('service:ControllerService')
    queue = TorqueBox.fetch('/queue/controller')
    render_injections(service, queue)
  end
end
