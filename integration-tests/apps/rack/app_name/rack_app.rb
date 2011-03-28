class RackApp
  def call(env)
    [200, {'Content-Type' => 'text/html'}, "constant:#{TORQUEBOX_APP_NAME}|env:#{ENV['TORQUEBOX_APP_NAME']}" ]
  end
end
