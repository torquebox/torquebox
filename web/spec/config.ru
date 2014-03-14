run lambda { |env|
  [200, { 'Content-Type' => 'text/plain' }, ["config.ru\n"]]
}
