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

package org.torquebox.core.injection.jndi;

import org.torquebox.core.injection.analysis.AbstractInjectableHandler;
import org.torquebox.core.injection.analysis.Injectable;

/** 
 * Handler for JNDI injectables.
 * 
 * <p>
 * This handler matches injections that are strings beginning with <code>java:</code>,
 * such as:
 * </p>
 * 
 * <pre>
 *   fetch( 'java:comp/env/whatever' )
 * </pre>
 * 
 * @author Bob McWhirter
 */
public class JNDIInjectableHandler extends AbstractInjectableHandler {
    
    public static final String TYPE = "jndi";

    public JNDIInjectableHandler() {
        super( TYPE );
        setRecognitionPriority( 10 * 1000 );
    }

    @Override
    public Injectable handle(Object injection, boolean generic) {
        String name = getString( injection );
        return new JNDIInjectable( name, generic );
    }

    @Override
    public boolean recognizes(Object injection) {
        String str = getString( injection );
        
        return (str != null) && str.startsWith(  "java:" );
    }


    
}
