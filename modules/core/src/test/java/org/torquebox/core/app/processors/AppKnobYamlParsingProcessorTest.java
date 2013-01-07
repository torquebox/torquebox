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

package org.torquebox.core.app.processors;

import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.test.as.AbstractDeploymentProcessorTestCase;

public class AppKnobYamlParsingProcessorTest extends AbstractDeploymentProcessorTestCase {
    

    @Before
    public void setUp() throws Throwable {
        clearDeployers();
        appendDeployer( new AppKnobYamlParsingProcessor() );
    }

    @Test(expected=DeploymentUnitProcessingException.class)
    public void testInvalidRootKnob() throws Exception {
        deployResourceAs( "invalid-root-knob.yml", "invalid-root-knob.yml" );
    }
    
    @Test
    public void testRootlessKnob() throws Exception {
        deployResourceAs( "rootless-knob.yml", "rootless-knob.yml" );
    }

}
