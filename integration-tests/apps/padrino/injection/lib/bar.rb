class Bar
  include TorqueBox::Injectors

  def service
    fetch('service:BarService')
  end

  def queue
    fetch('/queue/bar')
  end
end
