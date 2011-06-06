package org.torquebox.core.injection.analysis;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.msc.service.ServiceName;

/**
 * Injectable item which translates to an acceptable MSC {@link ServiceName}.
 * 
 * @see InjectableHandler
 * @see ServiceName
 * 
 * @author Bob McWhirter
 */
public interface Injectable {

    /**
     * Retrieve the type of the injectable, typically related-to, if not
     * identical-to, the originating {@link InjectableHandler}'s type.
     * 
     * @return
     */
    String getType();

    /**
     * Retrieve the specific name of this injectable.
     * 
     * @return The name.
     */
    String getName();

    /**
     * Retrieve the lookup key for this injectable.
     * 
     * <p>
     * The key may not match the name, given the way expressions evaluate at
     * runtime within the Ruby * interpreter (specifically, how the constant
     * 'org.foo.Someclass' translates to a string.
     * </p>
     * 
     * @return The key used to index this injectable in the Ruby interpreter.
     */
    String getKey();

    boolean isGeneric();

    /**
     * Retrieve the MSC <code>ServiceName</code> of the actual underlying
     * injectable asset.
     * 
     * @param phaseContext The deployment context.
     * @return The <code>ServiceName</code> of the injectable item.
     * @throws Exception
     */
    ServiceName getServiceName(DeploymentPhaseContext phaseContext) throws Exception;

}
