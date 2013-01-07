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

package org.torquebox.test.as;

import org.projectodd.polyglot.test.as.MockDeploymentPhaseContext;
import org.projectodd.polyglot.test.as.MockDeploymentUnit;

public class AbstractDeploymentProcessorTestCase extends org.projectodd.polyglot.test.as.AbstractDeploymentProcessorTestCase {

    protected MockDeploymentUnit deployResourceAsTorqueboxYml(String resource) throws Exception {
        return deployResourceAs( resource, "torquebox.yml" );   
    }   

    protected MockDeploymentPhaseContext setupResourceAsTorqueboxYml(String resource) throws Exception {
        return setupResourceAs( resource, "torquebox.yml" );
    }
}
