class Foo
  include TorqueBox::Injectors

  def service
    inject('service:FooService')
  end

  def queue
    inject('/queue/foo')
  end
end
