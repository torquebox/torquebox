require 'torquebox'
require 'somethingstrange'

module TheModule
  class TheClass
    
    include Enumerable
    include TorqueBox::Injectors
    include ::SomethingElse

    hash19 = { a: 'b' }
    
    def initialize()
      @random = inject_mc('jboss.whatever.Thing')
    end
    
    def do_something()
      @something = inject_jndi('java:/comp/whatever' )
    end
    
    def another_method() 
      @another = inject_cdi( com.mycorp.mypackage.MyThing )
    end
  end
end
