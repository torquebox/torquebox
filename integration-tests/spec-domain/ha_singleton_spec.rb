require 'spec_helper_domain'
require 'torquebox-messaging'

describe 'ha singleton services' do

  deploy <<-END.gsub(/^ {4}/,'')

    application:
      root: #{File.dirname(__FILE__)}/../apps/alacarte/ha-services
      env: development
    ruby:
      version: #{RUBY_VERSION[0,3]}

  END

  it 'should failover' do
    stop_server(:server1)
    ensure_service_master(:server2)
    start_server(:server1)
    stop_server(:server2)
    ensure_service_master(:server1)
    start_server(:server2)
  end

  def ensure_service_master(server)
    queue = TorqueBox::Messaging::Queue.new('/queues/node_name',
                                            :host => domain_host_for(server),
                                            :port => domain_port_for(server, 5445))
    condition = lambda { |message| message != nil }
    message = wait_for_condition(30, 1, condition) do
      queue.publish_and_receive('node_name', :timeout => 5000)
    end
    message.should == "master:#{domain_server_config_for(server)}"
  end

end

describe "ha singleton jobs" do

  deploy <<-END.gsub(/^ {4}/,'')

    application:
      root: #{File.dirname(__FILE__)}/../apps/alacarte/ha-jobs
      env: development
    ruby:
      version: #{RUBY_VERSION[0,3]}

  END

  it 'should failover' do
    stop_server(:server1)
    ensure_job_master(:server2)
    start_server(:server1)
    stop_server(:server2)
    ensure_job_master(:server1)
    start_server(:server2)
  end

  def ensure_job_master(server)
    queue = TorqueBox::Messaging::Queue.new('/queues/node_name',
                                            :host => domain_host_for(server),
                                            :port => domain_port_for(server, 5445))
    expected_message = "master:#{domain_server_config_for(server)}"
    condition = lambda { |message| message == expected_message }
    message = wait_for_condition(30, 1, condition) do
      queue.publish_and_receive('node_name', :timeout => 5000)
    end
    message.should == expected_message
  end

end

