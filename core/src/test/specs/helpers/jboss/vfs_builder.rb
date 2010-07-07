#import org.jboss.virtual.plugins.context.memory.MemoryContextFactory

import org.jboss.deployers.spi.structure.StructureMetaDataFactory

module JBoss
  
  class VFSBuilder
  
    def initialize(url)
      if ( url ) 
        @root_url = url
        @modifiable = false
      else 
        @root_url = "vfsmemory://test/"
        @modifiable = true
      end
      @root_vfs = VFS.getRoot( URL.new( @root_url ) )
      @stack = []
      @metadata_paths = []
      @structure = nil
    end
    
    def build_structure
      @structure = StructureMetaDataFactory.createStructureMetaData()
      context = StructureMetaDataFactory.createContextInfo()
      @metadata_paths.each do |path|
        context.addMetaDataPath( path ) 
      end
      @structure.addContext( context )
    end
    
    def structure
      build_structure unless @structure
      @structure 
    end
    
    def attach(cls, obj, name=nil)
    end
    
    def root_vfs
      @root_vfs
    end
    
    def show_root
      show_node( @root_vfs, '' ) 
    end
    
    def show_node(node, indent)
      # puts "#{indent}#{node.getPathName()}"
      node.getChildren().each do |child|
        show_node( child, "  #{indent}") 
      end
    end
    
    def root(opts={}, &block)
      dir( '', opts, &block ) 
    end
    
    def dir(name, opts={}, &block)
      @stack.push name 
      begin
        if ( opts[:metadata] ) 
          @metadata_paths << current_path 
        end
        context_factory.createDirectory( URL.new( current_url ) )
        instance_eval( &block ) if block 
      ensure
        @stack.pop
      end
    end
    
    def file(name, opts={}, &block)
      @stack.push name
      begin
        content = nil
        if ( opts[:read] ) 
          content = read_deployment_file( opts[:read] )
        else
          if ( block.nil? )
            content = ''
          else
            content = instance_eval( &block ).to_s
          end
        end
        
        ( content = '' ) unless content
        
        content_str = java.lang.String.new( content )
          
        context_factory.putFile( URL.new( current_url ), content_str.getBytes() )
      ensure
        @stack.pop
      end
    end
    
    def read_deployment_file(path)
      File.read( "#{BASE_DIR}/src/test/resources/deployments/#{path}") 
    end
    
    def current_path
      @stack.join( "/" ) 
    end
    
    def current_url
      @root_url + current_path()
    end
    
    def context_factory
      Java::OrgJbossVirtualPluginsContextMemory::MemoryContextFactory.getInstance()
    end
  end
end
