TorqueBox.configure do
  queue '/queues/inject-unprocessed'

  injection do
    enabled true
    path "foo", "bar"
  end
end
