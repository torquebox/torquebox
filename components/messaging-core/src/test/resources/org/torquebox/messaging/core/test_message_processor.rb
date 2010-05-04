
class TestMessageProcessor
  
  CONFIG_ONE = Marshal.dump( { :prop1=>"cheese", :prop2=>42 } )
  
  attr_accessor :opts
  
  def initialize()
    @opts = {}
  end
  
  def configure(opts)
    @opts = opts    
  end
  
end