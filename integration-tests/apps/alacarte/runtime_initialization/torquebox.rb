TorqueBox.configure do
  queue '/queues/tb_init_test' do
    durable false 
  end
  service SimpleService 
end
