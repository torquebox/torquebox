module ThreadSafe
  module Util
    # An overhead-less atomic reference.
    AtomicReference =
      if defined?(Rubinius::AtomicReference)
        Rubinius::AtomicReference
      else
        require 'atomic'
        defined?(Atomic::InternalReference) ? Atomic::InternalReference : Atomic
      end
  end
end