package org.torquebox.core.injection.analysis;

import java.util.Comparator;

import org.jruby.ast.Node;

/** Handle capable of recognizing and configuring injection for a class of injectable.
 *  
 * @author Bob McWhirter
 */
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
    
    /** Retrieve the type of the handler.
     * 
     * @return The type of the handler.
     */
    String getType();
    
    /** Determine if this handler recognizes the argument.
     * 
     * @param argsNode The argument AST.
     * @return <code>true</code> if this handler recognizes the argument, otherwise <code>false</code>.
     */
    boolean recognizes(Node argsNode);
    
    /** Handle injection for an argument.
     * 
     * @param node The argument AST.
     * @param generic Denotes if this is a generic or explicit injection of this type. (Unused?)
     * @return The resulting injectable.
     */
    Injectable handle(Node node, boolean generic);
    
    /** The handler's priority for #recognizes. 
     * 
     * Lower numbers (including negatives) fire first.
     * Default is 0.
     */
    int getRecognitionPriority();

}
