/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.torquebox.messaging;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.hornetq.jms.server.JMSServerManager;
import org.jboss.as.naming.MockContext;
import org.jboss.as.naming.NamingStore;
import org.jboss.as.naming.ValueManagedReferenceFactory;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.naming.service.BinderService;
import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.jboss.msc.value.Values;

/**
 * Service responsible for creating and destroying a {@code javax.jms.Topic}.
 * 
 * @author Emanuel Muckenhuber
 */
public class JMSTopicService implements Service<Void> {

    private final InjectedValue<JMSServerManager> jmsServer = new InjectedValue<JMSServerManager>();

    private final String name;
    private final String[] jndi;

    public JMSTopicService(String name, String[] jndi) {
        this.name = name;
        this.jndi = jndi;
    }

    /** {@inheritDoc} */
    public synchronized void start(StartContext context) throws StartException {
        final JMSServerManager jmsManager = jmsServer.getValue();
        try {
            MockContext.pushBindingTrap();
            try {
                jmsManager.createTopic( false, name, jndi );
            } finally {
                final ServiceTarget target = context.getChildTarget();
                final Map<String, Object> bindings = MockContext.popTrappedBindings();
                for (Map.Entry<String, Object> binding : bindings.entrySet()) {
                    String bindingKey = bindingKeyFromBrokenMockContext( binding.getKey() );
                    final BinderService binderService = new BinderService( bindingKey );
                    target.addService( ContextNames.JAVA_CONTEXT_SERVICE_NAME.append( bindingKey ), binderService )
                            .addDependency( ContextNames.JAVA_CONTEXT_SERVICE_NAME, NamingStore.class, binderService.getNamingStoreInjector() )
                            .addInjection( binderService.getManagedObjectInjector(), new ValueManagedReferenceFactory( Values.immediateValue( binding.getValue() ) ) )
                            .setInitialMode( ServiceController.Mode.ACTIVE )
                            .install();
                }
            }
        } catch (Exception e) {
            throw new StartException( "failed to create queue", e );
        }
    }

    protected String bindingKeyFromBrokenMockContext(String key) {
        List<String> jndiList = Arrays.asList( this.jndi );
        if (jndiList.contains( key )) {
            // Key given back matches one of our JNDI bindings passed in
            // so it wasn't munged
            return key;
        } else {
            // The key given back to us was not in our original JNDI bindings,
            // check to see if MockContext stripped a leading slash off
            if (jndiList.contains( "/" + key )) {
                return "/" + key;
            } else {
                return key;
            }
        }
    }

    /** {@inheritDoc} */
    public synchronized void stop(StopContext context) {
        final JMSServerManager jmsManager = jmsServer.getValue();
        try {
            jmsManager.destroyTopic( name );
        } catch (Exception e) {
            Logger.getLogger( "org.jboss.messaging" ).warnf( e, "failed to destroy jms topic: %s", name );
        }
        // FIXME This shouldn't be here
        for(final String jndiBinding : jndi) {
            ServiceController<?> service = context.getController().getServiceContainer().getService(ContextNames.JAVA_CONTEXT_SERVICE_NAME.append(jndiBinding));
            if (service != null) {
                service.setMode(ServiceController.Mode.REMOVE);
            }
        }
    }

    /** {@inheritDoc} */
    public Void getValue() throws IllegalStateException {
        return null;
    }

    InjectedValue<JMSServerManager> getJmsServer() {
        return jmsServer;
    }

}
