class Foo
  include TorqueBox::Injectors

  def service
    fetch('service:FooService')
  end

  def queue
    fetch('/queue/foo')
  end
end
