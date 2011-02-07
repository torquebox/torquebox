
module TorqueBox
  class Kernel

    def self.kernel=(kernel)
      @kernel = kernel 
      blocks.keys.each do |name|
        blocks.delete(name).each do |block|
          lookup name, &block
        end
      end
      @kernel
    end

    def self.lookup(name, &block)
      if @kernel.nil?
        self.blocks[name] << block
        nil
      else
        entry = @kernel.getRegistry().findEntry(name)      
        return nil unless entry
        if block_given?
          yield entry.getTarget()
        else
          entry.getTarget()
        end
      end
    end

    def self.blocks
      @blocks ||= Hash.new{|h, k| h[k] = []}
    end

  end
end
