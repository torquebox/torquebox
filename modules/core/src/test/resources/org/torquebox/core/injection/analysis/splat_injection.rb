require 'torquebox'
require 'somethingstrange'

module TheModule
  class TheClass
  
    incs = [Enumerable, TorqueBox::Injectors, ::SomethingElse]
    include *incs
    
    def initialize()
      @random = inject_service('org.jboss.whatever.Thing')
    end
    
    def do_something()
      @something = inject_jndi('java:/comp/whatever' )
    end
    
  end
end
