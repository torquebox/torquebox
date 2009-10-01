
module Kernel

  alias_method :require_without_vfs, :require
  alias_method :load_without_vfs,    :load

  def require(str)
    puts "require(#{str})"
    require_without_vfs(str)
  end

  def load(str,wrap=false)
    puts "load(#{str},#{wrap})"
    load_without_vfs(str,wrap)
  end
end
