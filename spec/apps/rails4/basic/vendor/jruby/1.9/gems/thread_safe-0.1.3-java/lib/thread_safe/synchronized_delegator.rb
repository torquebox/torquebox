# This class provides a trivial way to synchronize all calls to a given object
# by wrapping it with a Delegator that performs Mutex#lock/unlock calls around
# the delegated #send. Example:
#
#   array = [] # not thread-safe on many impls
#   array = MutexedDelegator.new(array) # thread-safe
#
# A simple Mutex provides a very coarse-grained way to synchronize a given
# object, in that it will cause synchronization for methods that have no
# need for it, but this is a trivial way to get thread-safety where none may
# exist currently on some implementations.
#
# This class is currently being considered for inclusion into stdlib, via
# https://bugs.ruby-lang.org/issues/8556

require 'delegate'

unless defined?(SynchronizedDelegator)
  class SynchronizedDelegator < SimpleDelegator
    def initialize(*)
      super
      @mutex = Mutex.new
    end
    
    def method_missing(m, *args, &block)
      begin
        mutex = @mutex
	mutex.lock
	super
      ensure
	mutex.unlock
      end
    end
  end
end
