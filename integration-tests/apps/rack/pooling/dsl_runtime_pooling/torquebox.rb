TorqueBox.configure do
  pool :jobs do
    type :shared
    lazy false
  end

  pool :web do
    type :shared
    lazy false
  end

  pool :messaging do
    type :shared
    lazy false
  end
end