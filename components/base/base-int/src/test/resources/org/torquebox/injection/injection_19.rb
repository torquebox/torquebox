require 'torquebox'
require 'somethingstrange'

module TheModule
  class TheClass
    
    include Enumerable
    include TorqueBox::Injectors
    include ::SomethingElse

    hash19 = { a: 'b' }

    ->(a, b=nil, *c, d) { }

    lambda { |a, b=nil, *c, d| }
    
    def boo(a, b=nil, *c, d)
    end
   
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
