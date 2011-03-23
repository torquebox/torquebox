require 'torquebox'
require 'somethingstrange'

module TheModule
  class TheClass
  
    include Enumerable
    include TorqueBox::Injectors
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

    def logging_methods()
      @log_by_string = log( 'some name' )
      @log_by_class = log( com.mycorp.mypackage.MyThing )
    end
  end
end
