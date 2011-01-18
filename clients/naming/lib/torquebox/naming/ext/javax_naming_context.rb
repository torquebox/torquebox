
module javax.naming::Context

  def [](name)
    lookup(name)
  end

  def[]=(name, value)
    rebind(name, value)
  end

  def to_a
    list("").to_a.map {|i| i.name }
  end

end
