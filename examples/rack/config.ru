app = lambda { |env|
  [200, { 'Content-Type' => 'text/html' }, "Hello from Rack\n" ]
}

run app
