require 'torquebox'
require 'somethingstrange'

module TheModule
  class TheClass
  
    incs = [Enumerable, TorqueBox::Injectors, ::SomethingElse]
    include *incs
    
    def initialize()
      @random = fetch_msc('org.jboss.whatever.Thing')
    end
    
    def do_something()
      @something = fetch_jndi('java:/comp/whatever' )
    end
    
  end
end
