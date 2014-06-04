run lambda { |env|
  [200, { 'Content-Type' => 'text/plain' }, ["other_config.ru\n"]]
}
