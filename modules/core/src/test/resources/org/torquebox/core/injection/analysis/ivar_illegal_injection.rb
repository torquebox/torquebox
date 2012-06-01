require 'torquebox'

module TheModule
  class TheClass
  
    include TorqueBox::Injectors
    
    def initialize()
      lookup(@foo)
    end
    
  end
end
