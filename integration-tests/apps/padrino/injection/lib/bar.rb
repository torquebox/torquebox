class Bar
  include TorqueBox::Injectors

  def service
    inject('service:BarService')
  end

  def queue
    inject('/queue/bar')
  end
end
