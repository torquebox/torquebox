require 'torquebox'
require 'somethingstrange'

module TheModule
  class TheClass
    
    include Enumerable
    include TorqueBox::Injectors
    include ::SomethingElse

    hash19 = { a: 'b' }

    ->(a, b=:stabbylambda, *c, d) {
      fetch_jndi('java:/some/hidden/thing')
    }

    lambda { |a, b=:lambda, *c, d|
      x = a
    }
    
    def boo(a, b=:def, *c, d)
      y = a
    end
   
    def initialize()
      @random = fetch_msc('org.jboss.whatever.Thing')
    end
    
    def do_something()
      @something = fetch_jndi('java:/comp/whatever' )
    end
    
  end
end
