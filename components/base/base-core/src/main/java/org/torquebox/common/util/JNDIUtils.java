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
package org.torquebox.common.util;

import java.util.Hashtable;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Basic utility for getting hold of a JNDI context on a specified host using
 * default settings.
 * @author peteroyle
 */
public class JNDIUtils {
    
    public static final String FACTORY = "org.jnp.interfaces.NamingContextFactory";
    public static final String KEY_FACTORY = "java.naming.factory.initial";
    public static final String KEY_URL_PKGS = "java.naming.factory.url.pkgs";
    public static final String PORT = ":1099";
    public static final String PROTOCOL = "jnp://";
    public static final String KEY_PROVIDER_URL = "java.naming.provider.url";
    public static final String URL_PKGS = "org.jboss.naming:org.jnp.interfaces";

    public static InitialContext getInitialContext(String host) throws NamingException {

        Hashtable props = new Hashtable();
        props.put(KEY_PROVIDER_URL, PROTOCOL + host + PORT);
        props.put(KEY_FACTORY, FACTORY);
        props.put(KEY_URL_PKGS, URL_PKGS);
        return new InitialContext(props);
    }

}
