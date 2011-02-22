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
package org.torquebox.auth;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.spi.dependency.KernelController;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

/**
 * Test the Authenticator bean
 */
public class AuthenticatorTest
{
    private Authenticator authenticator;

    @Before
    public void setUp() {
        authenticator = new Authenticator();    
    }

    @Test
    public void testAuthStrategy() {
        authenticator.setAuthStrategy("FooBar");
        assertEquals("FooBar", authenticator.getAuthStrategy());
    }

    @Test
    public void testDefaultAuthStrategy() {
        assertEquals(Authenticator.DEFAULT_AUTH_STRATEGY, authenticator.getAuthStrategy());
    }

    @Test
    public void testKernel() {
        KernelController kernelController = mock(KernelController.class);
        authenticator.setKernelController(kernelController);
        assertSame(kernelController, authenticator.getKernelController());
    }

    @Test
    public void testApplicationName() {
        authenticator.setApplicationName("MyApp");
        assertEquals("MyApp", authenticator.getApplicationName());
    }

    @Test
    public void testControllerInstall() throws Throwable {
        KernelController kernelController = mock(KernelController.class);
        authenticator.setKernelController(kernelController);
        authenticator.start();
        
        // TOD: Mockito doesn't like this for some reason
        // verify(kernelController).install(mock(BeanMetaData.class), new UsersRolesAuthenticator());
    }
}
