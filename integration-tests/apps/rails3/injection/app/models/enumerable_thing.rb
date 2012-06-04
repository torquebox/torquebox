class EnumerableThing
  include TorqueBox::Injectors
  include Enumerable
  
  def initialize
    @hash = { :a => :b }
    queue = fetch('/queues/injection_enumerable')

    inject('this should not barf') { |_, __| queue.publish('it worked') }
  end

  def each(&block)
    @hash.each(&block)
  end
end
