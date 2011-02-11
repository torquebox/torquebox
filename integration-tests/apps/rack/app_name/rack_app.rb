class RackApp
  def call(env)
    [200, {'Content-Type' => 'text/plain'}, "constant:#{TORQUEBOX_APP_NAME}|env:#{ENV['TORQUEBOX_APP_NAME']}" ]
  end
end
