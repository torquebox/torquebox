run lambda { |env|
    [200, {'Content-Type' => 'text/html'}, "HAM=#{ENV['HAM']}"]
}
