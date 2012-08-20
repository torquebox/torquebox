require 'something'

class RackApp
  def call(env)
    Something.define_foo if env['QUERY_STRING'] =~ /redefine/
    if env['QUERY_STRING'] =~ /bar/
      Something.new.background.bar
    else
      Something.new.foo
    end
    [200, {'Content-Type' => 'text/html'}, "it worked" ]
  end
end
