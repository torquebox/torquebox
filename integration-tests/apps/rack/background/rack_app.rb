require 'instance_methods_model'
require 'class_methods_model'

class RackApp
  def call(env)
    session = env['rack.session']
    if env['QUERY_STRING'] =~ /class_method/
      klass = ClassMethodsModel
      object = ClassMethodsModel
    else
      klass = InstanceMethodsModel
      object = InstanceMethodsModel.new
    end

    klass.define_foo if env['QUERY_STRING'] =~ /redefine/
    if env['QUERY_STRING'] =~ /bar/
      object.background.bar
    else
      future = object.foo
      session[:future] = future
    end
    if env['QUERY_STRING'] =~ /future_result/
      [200, {'Content-Type' => 'text/html'}, session[:future].result ]
    else
      [200, {'Content-Type' => 'text/html'}, "it worked" ]
    end
  end
end
