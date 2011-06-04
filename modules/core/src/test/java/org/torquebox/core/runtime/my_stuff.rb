

class MyStuff

  attr_accessor :name
  
  def initialize(name)
    @name = name
  end
  
  def receive(obj)
    puts "#{self} received #{obj.name} #{obj.class}"
    return obj
  end
  
end