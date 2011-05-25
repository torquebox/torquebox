package org.torquebox.core.injection;

import org.torquebox.core.injection.analysis.PredeterminedInjectableHandler;

/** Handles MSC service injections.
 * 
 * Priority: 4,000
 * 
 * @author Bob McWhirter
 */
public class CorePredeterminedInjectableHandler extends PredeterminedInjectableHandler {
    
    public CorePredeterminedInjectableHandler() {
        super( "predetermined-core" );
        setRecognitionPriority( 500 * 1000 );
        addInjectable( "service-registry", ServiceRegistryInjectable.INSTANCE );
        addInjectable( "service-target", ServiceTargetInjectable.INSTANCE );
    }

}