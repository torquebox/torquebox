require 'something'
require 'app/tasks/sample_task'

class RackApp
  def call(env)
    if env['QUERY_STRING'] =~ /background/
      something = Something.new
      20.times { something.foo }
    else
      20.times { SampleTask.async(:foo) }
    end
    [200, {'Content-Type' => 'text/html'}, "tasks fired" ]
  end
end
