
require 'torquebox'
require 'somethingstrange'

module TheModule
  class TheClass
  
    include Enumerable
    include TorqueBox::Injectors
    include ::SomethingElse
    
    def initialize()
    end
    
    def do_something()
      @something = inject('java:/comp/whatever' )
    end
    
    def another_method()
    end
    
    def some_messaging()
      @my_cue = inject( "/queues/mine" )
      @your_topic = inject( "/topics/yours" )
    end
  end
end