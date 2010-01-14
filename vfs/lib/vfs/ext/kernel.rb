
module Kernel

  private
  alias_method :open_without_vfs, :open

  def open(name, *rest, &block)
    if ( name =~ /^vfs(zip|file):/ )
      return IO.open( name, *rest, &block )
    end
    open_without_vfs( name, *rest, &block )
  end

end
