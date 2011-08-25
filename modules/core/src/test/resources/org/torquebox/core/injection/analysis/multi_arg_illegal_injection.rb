require 'torquebox'

module TheModule
  class TheClass
  
    include TorqueBox::Injectors
    
    def initialize()
      inject('something', 'somethingelse')
    end
    
  end
end
