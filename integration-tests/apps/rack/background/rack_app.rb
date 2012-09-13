require 'instance_methods_model'
require 'class_methods_model'

class RackApp
  def call(env)
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
      object.foo
    end
    [200, {'Content-Type' => 'text/html'}, "it worked" ]
  end
end
