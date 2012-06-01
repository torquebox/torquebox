require 'torquebox'

module TheModule
  class TheClass
  
    include TorqueBox::Injectors

    def initialize()
      fetch('something', 'somethingelse')
    end
    
  end
end
