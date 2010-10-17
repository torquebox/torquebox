
module FileTest

  class << self

    def directory?(filename)
      File.directory?(filename)
    end

    def exist?(filename)
      File.exist?(filename)
    end

    def exists?(filename)
      File.exists?(filename)
    end

    def file?(filename)
      File.file?(filename)
    end

    def readable?(filename)
      File.readable?(filename)
    end

    def writable?(filename)
      File.writable?(filename)
    end

  end

end
