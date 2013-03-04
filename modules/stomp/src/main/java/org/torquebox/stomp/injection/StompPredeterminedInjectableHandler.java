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

package org.torquebox.stomp.injection;

import org.torquebox.core.injection.analysis.PredeterminedInjectableHandler;

/** Handles MSC service injections.
 * 
 * Priority: 6000
 * 
 * @author Bob McWhirter
 */
public class StompPredeterminedInjectableHandler extends PredeterminedInjectableHandler {
    
    public StompPredeterminedInjectableHandler() {
        super( "stomp" );
        setRecognitionPriority( 600 * 1000 );
        addInjectable( "stomp-endpoint", StompEndpointBindingInjectable.INSTANCE );
        addInjectable( "stomp-endpoint-secure", StompEndpointBindingInjectable.SECURE_INSTANCE );
    }

}
