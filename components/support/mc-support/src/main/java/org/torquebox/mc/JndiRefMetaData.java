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

package org.torquebox.mc;

import javax.naming.Context;
import javax.naming.NamingException;

import org.jboss.beans.metadata.plugins.AbstractDependencyValueMetaData;
import org.jboss.reflect.spi.TypeInfo;

public class JndiRefMetaData extends AbstractDependencyValueMetaData {

    private static final long serialVersionUID = 1L;

    private static final String DEPENDS_JNDI_PREFIX = "naming:";

    private Context context;
    private String name;

    public JndiRefMetaData(Context context, String name) {
        log.info( "construct JNDI ref for " + context + " -> " + name );
        // System.err.println( "JndiRef Ctor" );
        this.context = context;
        this.name = name;
    }

    public Context getContext() {
        return this.context;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public Object getValue(TypeInfo info, ClassLoader cl) throws Throwable {
        log.info( "getValue() for " + this.name );
        return getValue();
    }

    @Override
    public Object getValue() {
        // System.err.println( "GETVALUE" );
        log.info( "getValue() for " + this.name );
        try {
            Object value = this.context.lookup( this.name );
            log.info( "value(" + this.name + ")=" + value );
            return value;
        } catch (NamingException e) {
            log.info( "naming exception: " + e );
            return null;
        }
    }

    @Override
    public Object getUnderlyingValue() {
        return DEPENDS_JNDI_PREFIX + getName();
    }

}
