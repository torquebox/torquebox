
TorqueBox.configure do

  job Another::SimpleJob do
    cron '*/1 * * * * ?'
    config do
      color 'blue'
      an_array [ 'one', 'two' ]
    end
  end

  queue '/queue/response',    :durable=>false 
  queue '/queue/init_params', :durable=>false 

end
