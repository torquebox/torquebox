package org.torquebox.core.injection.jndi;

import org.jboss.as.naming.ManagedReference;
import org.jboss.as.naming.ManagedReferenceFactory;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/** Service which handles lifecycle for <code>ManagedReference</code>-based
 * injections.
 * 
 * <p>This service obtains the reference before injection, and releases
 * it after un-injection, when the application is underployed.</p>
 * 
 * @author Bob McWhirter
 */
public class ManagedReferenceInjectableService implements Service<Object> {

    public ManagedReferenceInjectableService() {
        
    }
    
    @Override
    public Object getValue() throws IllegalStateException, IllegalArgumentException {
        return this.reference.getInstance();
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.reference = this.managedReferenceFactoryInjector.getValue().getReference();
    }

    @Override
    public void stop(StopContext context) {
        this.reference.release();
    }
    
    public Injector<ManagedReferenceFactory> getManagedReferenceFactoryInjector() {
        return this.managedReferenceFactoryInjector;
    }

    private ManagedReference reference;
    private InjectedValue<ManagedReferenceFactory> managedReferenceFactoryInjector = new InjectedValue<ManagedReferenceFactory>();
}
