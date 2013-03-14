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

import java.util.Comparator;

/** Handle capable of recognizing and configuring injection for a class of injectable.
 *  
 * @author Bob McWhirter
 */
public interface InjectableHandler {
    
    public Comparator<InjectableHandler> RECOGNITION_PRIORITY = new Comparator<InjectableHandler>() {

        @Override
        public int compare(InjectableHandler o1, InjectableHandler o2) {
            int p1 = o1.getRecognitionPriority();
            int p2 = o2.getRecognitionPriority();
            
            if ( p1 > p2 ) {
                return 1;
            }
            
            if ( p1 < p2 ) {
                return -1;
            }
            
            // if same priority, just sort by type.
            return o1.getType().compareTo( o2.getType() );
        }
    };
    
    /** Retrieve the type of the handler.
     * 
     * @return The type of the handler.
     */
    String getType();
    
    /** Determine if this handler recognizes the argument.
     * 
     * @param injection The injection object.
     * @return <code>true</code> if this handler recognizes the argument, otherwise <code>false</code>.
     */
    boolean recognizes(Object injection);
    
    /** Handle injection for an argument.
     * 
     * @param node The injection object.
     * @param generic Denotes if this is a generic or explicit injection of this type. (Unused?)
     * @return The resulting injectable.
     */
    Injectable handle(Object injection, boolean generic);
    
    /** The handler's priority for #recognizes. 
     * 
     * Lower numbers (including negatives) fire first.
     * Default is 0.
     */
    int getRecognitionPriority();

}
