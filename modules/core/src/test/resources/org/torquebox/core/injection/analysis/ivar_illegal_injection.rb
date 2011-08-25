require 'torquebox'

module TheModule
  class TheClass
  
    include TorqueBox::Injectors
    
    def initialize()
      inject(@foo)
    end
    
  end
end
