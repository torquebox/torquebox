
class Dir

  class << self

    #alias_method :open_before_vfs, :open
    alias_method :glob_before_vfs, :glob

    def open(str)
      result = dir = VFS::Dir.new( str )
      if block_given?
        begin
          result = yield( dir )
        ensure
          dir.close 
        end
      end
      result
    end

    def [](pattern)
      self.glob( pattern )
    end

    def glob(pattern,flags=nil)

      first_special = ( pattern =~ /[\*\?\[\{]/ )
      base          = pattern[0, first_special]

      if ( File.exist_without_vfs?( base ) && File.directory?( base ) )
        return glob_before_vfs( pattern )
      end

      original_base = base
      prefix = nil
      unless ( base =~ %r(^vfs[^:]+) )
        existing = VFS.first_existing( base )
        return [] unless existing
        is_archive = Java::OrgJbossVirtualPluginsContextJar::JarUtils.isArchive( File.basename( existing ) )
        if ( is_archive )
          prefix = "vfszip://#{Dir.pwd}"
          base = "#{prefix}/#{existing}/"
          matcher = pattern[ existing.length+1..-1 ]
        end
      else
        matcher = pattern[first_special..-1]
      end
      root = org.jboss.virtual.VFS.root( base[0..-1] )
      paths = root.children_recursively( VFS::GlobFilter.new( matcher ) ).collect{|e| 
        "#{base}#{e.path_name}"
      }
      paths = paths.collect{|fq_path|
        prefix ? fq_path[prefix.length+1..-1] : fq_path
      }
      paths
    end

  end
end 

