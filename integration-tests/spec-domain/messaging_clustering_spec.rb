require 'spec_helper'
require 'torquebox-messaging'

describe 'messaging clustering' do

  deploy <<-END.gsub(/^ {4}/, '')
    queues:
      /queues/clustering:
        durable: false
  END

  it 'should be able to publish on one server and receive on another' do
    server1 = TorqueBox::Messaging::Queue.new('/queues/clustering',
                                              :host => domain_host_for(:server1),
                                              :port => domain_port_for(:server1, 5445))
    server2 = TorqueBox::Messaging::Queue.new('/queues/clustering',
                                              :host => domain_host_for(:server2),
                                              :port => domain_port_for(:server2, 5445))

    server1.publish('a message')
    server2.receive(:timeout => 10_000).should == 'a message'
  end

end
