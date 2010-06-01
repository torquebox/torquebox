
module javax.naming::Context

  def [](name)
    lookup(name)
  end

  def[]=(name, value)
    rebind(name, value)
  end

end
