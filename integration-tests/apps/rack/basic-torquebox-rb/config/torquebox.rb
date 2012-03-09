TorqueBox.configure do
  environment 'biscuit' => 'gravy'
  
  environment { 
    HAM :biscuit
    FOO :bar
  }

  options_for :messaging, :default_message_encoding => :marshal_base64
  
  options_for Backgroundable, :disabled => true

  pool :foo, :type => :bounded, :min => 0, :max => 6

  pool :cheddar do
    type :bounded
    min 0
    max 6
  end

  job AJob, :name => :a_job, :cron => '*/1 * * * * ?'

  job AJob do
    cron '*/1 * * * * ?'
    config do 
      ham 'biscuit'
    end
  end
  queue '/queue/a-queue', :durable => false
  queue '/queue/another-queue', :durable => false
  
  queue '/queue/job-queue' do
    durable false
  end

  queue '/queue/another-queue', :durable => false do
    processor AProcessor, :concurrency => 2, :selector => "steak = 'salad'", :config => { :foo => :bar }
  end

  queue '/queue/yet-another-queue' do
    durable false
    processor AProcessor do
      concurrency 2
      selector "steak = 'salad'"
      config(:foo => :bar)
    end
  end

  queue '/queue/singleton-queue' do
    durable false
    processor AProcessor do
      singleton true
    end
  end

  topic '/topic/a-topic', :durable => false

  ruby :version => '1.9'

  service AService, :name => 'ham', :config => { :foo => :bar }

  service AnotherService do
    name 'biscuit'
    config do
      flavor 'with honey'
    end
  end

  service AnotherService
  
  web :context => '/torquebox-rb'

  authentication :ham, :domain => 'torquebox-auth'
  authentication :biscuit do
    domain 'torquebox-auth'
  end
end
