TorqueBox.configure do
  queue '/queues/tb_init_test' do
    durable false 
  end
  queue '/queues/service_context' do
    durable false 
  end
  queue '/queues/jobs_context' do
    durable false 
  end
  service SimpleService 
  job SimpleJob do
    cron '*/1 * * * * ?'
  end
end
