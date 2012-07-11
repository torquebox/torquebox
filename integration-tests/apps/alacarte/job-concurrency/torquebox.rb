TorqueBox.configure do
  options_for :jobs, :concurrency => 5
  
  5.times do |n|
    job MyJob do
      cron '*/5 * * * * ?'
      config(:number => n)
    end
  end

  pool :jobs, :type => :shared
  
  queue '/queue/backchannel'
end
