require 'pathname'

class Pathname

  alias_method :realpath_without_vfs, :realpath

  def realpath
    vfs_path? ? expand_path : realpath_without_vfs
  end

  def vfs_path?
    @path.to_s =~ /^vfs:/
  end
end
