require 'rack'

use Rack::Deflater

app = lambda do |env|
  [200, { 'Content-Type' => 'text/plain' }, ["deflate test\n"]]
end

run app
