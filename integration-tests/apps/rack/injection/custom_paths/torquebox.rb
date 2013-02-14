TorqueBox.configure do
  queue '/queues/inject-custom'

  injection do
    enabled true
    path "custom"
  end
end
