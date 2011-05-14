package org.torquebox.core.injection.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.vfs.VirtualFile;

public class InjectionIndex {
    
    public InjectionIndex() {
    }
    
    public void addInjectables(VirtualFile path, Set<Injectable> injectables) {
       Set<Injectable> existing = this.index.get( path ); 
       
       if ( existing == null ) {
           existing = new HashSet<Injectable>();
           this.index.put( path, existing );
       }
       
       existing.addAll(  injectables );
    }
    
    public String toString() {
        return this.index.toString();
    }
    
    private final Map<VirtualFile,Set<Injectable>> index = new HashMap<VirtualFile,Set<Injectable>>();
    
    public static final AttachmentKey<InjectionIndex> ATTACHMENT_KEY = AttachmentKey.create(InjectionIndex.class);
}
