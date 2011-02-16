require 'something'
require 'app/tasks/sample_task'

class RackApp
  def call(env)
    if env['QUERY_STRING'] =~ /background/
      something = Something.new
      something.foo(1)
      something.foo(2)
    else
      SampleTask.async(:foo, 1)
      SampleTask.async(:foo, 2)
    end
    [200, {'Content-Type' => 'text/plain'}, "tasks fired" ]
  end
end
