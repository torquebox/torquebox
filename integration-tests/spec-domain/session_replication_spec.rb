require 'spec_helper_domain'

shared_examples_for 'session replication' do
  it 'should set on one node and get on the other' do
    response = response_for(:server1, "#{context}/set_value")
    response['Set-Cookie'].should_not be_nil
    cookie = response['Set-Cookie']
    response.body.should include('the value')

    # session replication isn't synchronous
    sleep 2

    response = response_for(:server2, "#{context}/get_value") do |request|
      request.add_field('Cookie', cookie)
    end
    response['Set-Cookie'].should be_nil
    response.body.should include('the value')
  end
end

describe 'rails3 session replication' do
  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../apps/rails3/basic
      RAILS_ENV: development
    web:
      context: /rails-repl
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  let(:context) { '/rails-repl/sessioning' }

  it_should_behave_like 'session replication'
end

describe 'rack session replication' do
  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/sinatra/sessions
      env: development
    web:
      context: /sinatra-repl
    ruby:
      version: #{RUBY_VERSION[0,3]}

  END

  let(:context) { '/sinatra-repl' }

  it_should_behave_like 'session replication'
end

def response_for(server, path)
  base = "http://#{domain_host_for(server)}:#{domain_port_for(server, 8080)}"
  uri = URI.parse("#{base}#{path}")
  http = Net::HTTP.new(uri.host, uri.port)
  request = Net::HTTP::Get.new(uri.request_uri)
  # Set the HTTP Host header to simulate load balancing
  request['Host'] = 'localhost:8080'
  yield request if block_given?
  http.request(request)
end
