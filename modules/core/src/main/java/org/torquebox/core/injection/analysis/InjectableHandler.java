package org.torquebox.core.injection.analysis;

import java.util.Comparator;

import org.jruby.ast.Node;

public interface InjectableHandler {
    
    public Comparator<InjectableHandler> RECOGNITION_PRIORITY = new Comparator<InjectableHandler>() {

        @Override
        public int compare(InjectableHandler o1, InjectableHandler o2) {
            int p1 = o1.getRecognitionPriority();
            int p2 = o2.getRecognitionPriority();
            
            if ( p1 > p2 ) {
                return 1;
            }
            
            if ( p1 < p2 ) {
                return -1;
            }
            
            // if same priority, just sort by type.
            return o1.getType().compareTo( o2.getType() );
        }
    };
    String getType();
    Injectable handle(Node node, boolean generic);
    
    /** The handler's priority for #recognizes. 
     * 
     * Lower numbers (including negatives) fire first.
     * Default is 0.
     */
    int getRecognitionPriority();
    boolean recognizes(Node argsNode);

}
