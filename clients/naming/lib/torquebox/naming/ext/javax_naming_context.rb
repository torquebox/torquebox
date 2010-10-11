
module javax.naming::Context

  def [](name)
    puts "lookup(#{name})"
    lookup(name)
  end

  def[]=(name, value)
    puts "rebind(#{name}, #{value})"
    rebind(name, value)
  end

end
