require 'torquebox'
require 'somethingstrange'

module TheModule
  class TheClass
    torque {
   		@random = mc('jboss.whatever.Thing')
   		@something = jndi('java:/comp/whatever' )
   		@another = cdi( com.mycorp.mypackage.MyThing )
    }
  end
end