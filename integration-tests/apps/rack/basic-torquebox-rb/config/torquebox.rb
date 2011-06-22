TorqueBox.configure do
  env :ham

  environment 'biscuit' => 'gravy'

  options_for Backgroundable, :disabled => true

  pool :foo, :type => :bounded, :min => 0, :max => 6

  job :a_job, :class => AJob, :cron => '*/1 * * * * ?'
  
  queue '/queue/a-queue', :durable => false

  queue '/queue/another-queue', :durable => false do
    processor AProcessor, :concurrency => 2, :filter => "steak = 'salad'", :config => { :foo => :bar }
  end
  
  topic '/topic/a-topic', :durable => false

  ruby :version => '1.9'

  service 'a-service', :class => AService, :config => { :foo => :bar }
  
  web :context => '/torquebox-rb'

  authentication :ham, :domain => 'torquebox-auth'
  authentication :biscuit, :domain => 'torquebox-auth'
end
