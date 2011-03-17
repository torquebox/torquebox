require 'torquebox'
require 'somethingstrange'

module TheModule
  class TheClass
    torque {
   		inject :random, mc('jboss.whatever.Thing')
   		inject :something, jndi('java:/comp/whatever' )
    }
  end
end