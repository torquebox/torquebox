run lambda { |env|
  [200, { 'Content-Type' => 'text/plain' }, ["sockjs echo example\n"]]
}
