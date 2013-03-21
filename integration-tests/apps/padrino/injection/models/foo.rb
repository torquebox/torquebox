class Foo

  def service
    TorqueBox.fetch('service:FooService')
  end

  def queue
    TorqueBox.fetch('/queue/foo')
  end
end
