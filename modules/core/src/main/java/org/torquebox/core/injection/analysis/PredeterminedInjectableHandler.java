/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

package org.torquebox.core.injection.analysis;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jboss.msc.service.ServiceTarget;

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
    public Injectable handle(Object injection, boolean generic) {
        String key = getString( injection );
        return this.injectables.get( key );
    }

    @Override
    public boolean recognizes(Object injection) {
        String key = getString( injection );
        return this.injectables.containsKey( key );
    }

    public Collection<Injectable> getInjectables() {
        return this.injectables.values();
    }

    private final Map<String, Injectable> injectables = new HashMap<String, Injectable>();
}
