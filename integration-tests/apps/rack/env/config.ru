app = lambda { |env|
  key = env['PATH_INFO'].sub('/', '')
  value = env[key]
  value = ENV[key] if value.nil?
  [200, { 'Content-Type' => 'text/plain' }, [value]]
}
run app
