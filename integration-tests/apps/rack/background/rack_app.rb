require 'something'

class RackApp
  def call(env)
    Something.new.foo
    [200, {'Content-Type' => 'text/plain'}, "it worked" ]
  end
end
