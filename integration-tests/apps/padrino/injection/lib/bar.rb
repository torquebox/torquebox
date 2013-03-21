class Bar

  def service
    TorqueBox.fetch('service:BarService')
  end

  def queue
    TorqueBox.fetch('/queue/bar')
  end
end
