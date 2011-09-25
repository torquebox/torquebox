package org.torquebox.core.runtime;

import org.jboss.as.naming.context.NamespaceContextSelector;
import org.jboss.logging.Logger;

public class ThreadManager {
    
    public static ThreadContextBundle getCurrentBundle() {
        log.info( "getCurrentBundle() " + Thread.currentThread() );
        return new ThreadContextBundle( NamespaceContextSelector.getCurrentSelector() );
    }
    
    public static void prepareThread(ThreadContextBundle parentBundle) {
        log.info( "prepareThread() " + Thread.currentThread() );
        NamespaceContextSelector.pushCurrentSelector( parentBundle.getNamespaceContextSelector() );
    }
    
    public static void unprepareThread(ThreadContextBundle parentBundle) {
        log.info( "unprepareThread() " + Thread.currentThread() );
        NamespaceContextSelector.popCurrentSelector();
    }
    
    public static class ThreadContextBundle {
        
        private NamespaceContextSelector selector;

        public ThreadContextBundle(NamespaceContextSelector selector) {
            this.selector = selector;
        }
        
        public NamespaceContextSelector getNamespaceContextSelector() {
            return this.selector;
        }
        
    }
    
    private static final Logger log = Logger.getLogger( "org.torquebox.core.runtime.thread"  );

}
