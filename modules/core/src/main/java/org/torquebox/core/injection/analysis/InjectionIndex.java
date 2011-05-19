package org.torquebox.core.injection.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.vfs.VirtualFile;

public class InjectionIndex {
	public static final AttachmentKey<InjectionIndex> ATTACHMENT_KEY = AttachmentKey.create(InjectionIndex.class);
	
    public InjectionIndex(VirtualFile root) {
        this.root = root;
    }
    
    public void addInjectables(VirtualFile path, Set<Injectable> injectables) {
       Set<Injectable> existing = this.index.get( path ); 
       
       if ( existing == null ) {
           existing = new HashSet<Injectable>();
           this.index.put( path.getPathNameRelativeTo( this.root ), existing );
       }
       
       existing.addAll(  injectables );
    }
    
    public Set<Injectable> getInjectablesFor(List<String> pathPrefixes) {
        Set<Injectable> injectables = new HashSet<Injectable>();
        
        for ( Entry<String, Set<Injectable>> entry : this.index.entrySet() ) {
            for ( String prefix : pathPrefixes ) {
            	String key = entry.getKey();
            	// root ('.') is a special case - only files in the root are matched
                if ( key.startsWith( prefix ) ||
                		(".".equals( prefix ) && !key.contains( "/" ))) {	
                    injectables.addAll( entry.getValue() );
                }
            }
        }
        return injectables;
    }
    
    public String toString() {
        return this.index.toString();
    }
    
    private final VirtualFile root;
    private final Map<String,Set<Injectable>> index = new HashMap<String,Set<Injectable>>();
    
}
