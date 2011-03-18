require 'torquebox'
require 'somethingstrange'

module TheModule
  class TheClass
    torque {
   		@random = mc('jboss.whatever.Thing')
   		@something = jndi('java:/comp/whatever' )
    }
  end
end