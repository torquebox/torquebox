require 'spec_helper'

def domain_server
  Thread.current[:app_server]
end

def domain_host_for(server)
  'localhost'
end

def domain_port_for(server, base_port)
  port_offset = 100
  server == :server1 ? base_port : base_port + port_offset
end

def domain_server_config_for(server)
  server == :server1 ? 'server-01' : 'server-02'
end

def start_server(server)
  server_config = domain_server_config_for(server)
  domain_server.send(:api, :operation => 'start',
                     :address => [{ :host => 'master' },
                                  { 'server-config' => server_config }])
  wait_for_status(server_config, 'STARTED', 30)
  # Sleep to give the application time to deploy after the server
  # has started
  sleep 20
end

def stop_server(server)
  server_config = domain_server_config_for(server)
  domain_server.send(:api, :operation => 'stop',
                     :address => [{ :host => 'master' },
                                  { 'server-config' => server_config }])
  wait_for_status(server_config, 'STOPPED', 30)
end

def wait_for_status(server_config, expected_status, timeout)
  condition = lambda { |status| status == expected_status }
  wait_for(timeout, 2, condition) do
    server_status(server_config)
  end
end

def server_status(server_config)
  JSON.parse(domain_server.send(:api, :operation => 'read-resource',
                                :address => [{ :host => 'master' },
                                             { 'server-config' => server_config }],
                                'include-runtime' => true))['result']['status']
rescue
  nil
end
