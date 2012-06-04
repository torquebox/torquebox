require 'torquebox'
require 'somethingstrange'

module TheModule
  class TheClass
  
    include Enumerable
    include TorqueBox::Injectors
    include ::SomethingElse
    
    def initialize()
      @random = fetch_msc('jboss.whatever.Thing')
    end
    
    def do_something()
      @something = fetch_jndi('java:/comp/whatever' )
    end
    
    def broken()
      @taco = fetch_tacos( "whatnot" )
    end
    
    def another_method() 
      @another = fetch_cdi( com.mycorp.mypackage.MyThing )
    end
  end
end
