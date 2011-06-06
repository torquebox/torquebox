package org.torquebox.core.injection.analysis;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jruby.ast.Node;

/**
 * Injectable-handler which can provide injectables to all deployments
 * regardless of specific configuration.
 * 
 * <p>
 * Predetermined injectables are injectables that may be assumed to be globally
 * available for all deployments. Examples include AS-global items, such as
 * connection-managers, or deployment-global items, such as an MSC
 * {@link ServiceTarget} for the deployment.
 * </p>
 * 
 * @author Bob McWhirter
 * 
 */
public class PredeterminedInjectableHandler extends AbstractInjectableHandler {

    /**
     * Construct for a subsystem.
     * 
     * @param subsystemName
     *            The subsystem name.
     */
    public PredeterminedInjectableHandler(String subsystemName) {
        super( "predetermined_" + subsystemName );
    }

    /**
     * Add a predetermined injectable.
     * 
     * <p>Typically this method would be called one-or-more times
     * from the constructor of a subclass, or immediately after
     * constructing an instance of this class, before registering
     * it with the {@link InjectableHandlerRegistry}.
     * 
     * @param name The name of the injectable.
     * @param injectable The injectable itself.
     */
    protected void addInjectable(String name, Injectable injectable) {
        this.injectables.put( name, injectable );
    }

    @Override
    public Injectable handle(Node node, boolean generic) {
        String key = getString( node );
        return this.injectables.get( key );
    }

    @Override
    public boolean recognizes(Node argsNode) {
        String key = getString( argsNode );
        return this.injectables.containsKey( key );
    }

    public Collection<Injectable> getInjectables() {
        return this.injectables.values();
    }

    private final Map<String, Injectable> injectables = new HashMap<String, Injectable>();
}
