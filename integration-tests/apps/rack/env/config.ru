app = lambda { |env|
  key = env['PATH_INFO'].sub('/', '')
  [200, { 'Content-Type' => 'text/plain' }, [env[key]]]
}
run app
