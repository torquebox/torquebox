/* Copyright 2010 Red Hat, Inc. */
package org.torquebox.common.spi;

import org.jboss.beans.metadata.api.annotations.Create;

/**
 * Generic interface for simple factories.
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 * 
 * @param <T>
 *            The instance type.
 */
public interface InstanceFactory<T> {

    /**
     * Create an instance.
     * 
     * @return The newly-created instance.
     * @throws Exception
     *             if an error occurred attempting to create the instance.
     */
    @Create(ignored = true)
    T create() throws Exception;

    void dispose(T instance);

}
