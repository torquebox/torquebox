require 'torquebox'
require 'somethingstrange'

module TheModule
  class TheClass
  
    include Enumerable
    include ::SomethingElse
    
    def initialize()
      @random = mc('jboss.whatever.Thing')
    end
    
    def do_something()
      @something = jndi('java:/comp/whatever' )
    end
    
    def another_method() 
      @another = cdi( com.mycorp.mypackage.MyThing )
    end
  end
end
