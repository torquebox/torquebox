require 'torquebox'

module TheModule
  class TheClass
  
    include TorqueBox::Injectors
    
    def initialize()
      fetch(@foo)
    end
    
  end
end
