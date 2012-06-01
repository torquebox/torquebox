require 'torquebox'

module TheModule
  class TheClass
  
    include TorqueBox::Injectors

    def initialize()
      lookup('something', 'somethingelse')
    end
    
  end
end
