package org.torquebox.core.injection;

import org.torquebox.core.injection.analysis.PredeterminedInjectableHandler;

/**
 * Handler for predetermined injectables for the <b>core</b> subsystem.
 * 
 * <p>
 * This handler provides injection support for {@link ServiceRegistry} and
 * {@link ServiceTarget}.
 * </p>
 * 
 * <p>
 * The <code>ServiceRegistry</code> may be injected as
 * <code>service-registry</code>, while the <code>ServiceTarget</code>
 * (deployment-scoped) may be injected as <code>service-target</code>.
 * </p>
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