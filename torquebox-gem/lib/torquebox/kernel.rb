
module TorqueBox
  class Kernel
    def self.kernel=(kernel)
      @kernel = kernel 
    end

    def self.lookup(name)
      entry = @kernel.getRegistry().findEntry(name)      
      return nil unless entry
      entry.getTarget()
    end
  end
end
