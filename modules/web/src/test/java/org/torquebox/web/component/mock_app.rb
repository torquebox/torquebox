

class MockApp
  attr_accessor :env
  def call(env)
    @env = env
    return [ 200, 'text-plain', '' ]
  end
  
end