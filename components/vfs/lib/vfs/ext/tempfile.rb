require 'tempfile'
require 'tmpdir'

class Tempfile
  class << self

    alias_method :new_without_vfs, :new

    def new(basename, tmpdir=Dir.tmpdir)
      new_without_vfs(basename, File.name_without_vfs(tmpdir))
    end

  end
end
