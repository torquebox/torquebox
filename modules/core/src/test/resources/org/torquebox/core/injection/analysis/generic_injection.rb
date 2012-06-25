require 'torquebox'
require 'somethingstrange'

module TheModule
  class TheClass
  
    include Enumerable
    include TorqueBox::Injectors
    include ::SomethingElse
    
    def initialize()
      @random = fetch('jboss.web:service=WebServer')
    end
    
    def do_something()
      @something = fetch('java:/comp/whatever' )
    end
    
    def another_method() 
      @another = fetch( com.mycorp.mypackage.MyThing )
    end
    
  end
end
