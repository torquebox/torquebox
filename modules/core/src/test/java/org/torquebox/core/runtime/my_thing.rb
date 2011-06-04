

class MyThing

  attr_accessor :name
  
  def initialize(name)
    @name = name
  end
  
  def receive(obj)
    puts "#{self} received #{obj.name} #{obj.class}"
    another = obj.class.new( "another" )
    puts "created another #{another.name}"
    return another
  end
  
end