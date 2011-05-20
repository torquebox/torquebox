package org.torquebox.messaging.injection;

import org.torquebox.core.injection.analysis.PredeterminedInjectableHandler;

/** Handles MSC service injections.
 * 
 * Priority: 4,000
 * 
 * @author Bob McWhirter
 */
public class MessagingPredeterminedInjectableHandler extends PredeterminedInjectableHandler {
    
    public MessagingPredeterminedInjectableHandler() {
        super( "messaging" );
        setRecognitionPriority( 500 * 1000 );
        addInjectable( "connection-factory", ConnectionFactoryInjectable.INSTANCE );
    }

}