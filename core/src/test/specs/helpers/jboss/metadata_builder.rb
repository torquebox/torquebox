#import org.jboss.virtual.plugins.context.memory.MemoryContextFactory

require 'helpers/jboss/vfs_builder'
import org.jboss.deployers.vfs.spi.client .VFSDeploymentFactory

module JBoss
  
  class MetadataBuilder
  
    def initialize(root, &block)
      @root = root
      @entries = []
      attachments( &block )
    end
    
    def attachments(&block)
      instance_eval( &block ) if block
    end
    
    def attach(cls, name=nil, &block)
      java_class = cls.java_class 
      object = cls.new
      block.call( object, @root ) if block
      @entries << Entry.new( java_class, object, name )
    end
    
    def attach_object(cls, object)
      java_class = cls.java_class 
      @entries << Entry.new( java_class, object, nil )
    end
    
    def attach_to(deployment)
      
      @entries.each do |entry|
        #puts "attaching [#{entry.name}, #{entry.object}, #{entry.java_class}"
        if ( entry.name.nil? ) 
          deployment.getPredeterminedManagedObjects().addAttachment( entry.java_class, entry.object )
        else
          deployment.getPredeterminedManagedObjects().addAttachment( entry.name, entry.object, entry.java_class )
        end
      end
    end
    
    class Entry
      
      attr_accessor :name
      attr_accessor :java_class
      attr_accessor :object
      
      def initialize(java_class, object, name=nil)
        @java_class = java_class
        @object     = object
        @name       = name
      end
      
    end
  end
end