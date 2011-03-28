require 'something'

class RackApp
  def call(env)
    Something.define_foo if env['QUERY_STRING'] =~ /redefine/
    Something.new.foo
    [200, {'Content-Type' => 'text/html'}, "it worked" ]
  end
end
