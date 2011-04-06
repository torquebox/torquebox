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

package org.torquebox.interp.core;

import org.jruby.Ruby;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.test.mc.vfs.AbstractVFSTestCase;

public class VFSLoadServiceWithRuntimeTest extends AbstractVFSTestCase {

    private VFSLoadService loadService;
    private Ruby ruby;

    @Before
    public void setUp() throws Exception {
        RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl();
        factory.setUseJRubyHomeEnvVar( false );
        this.ruby = factory.createInstance( "test" );
        this.loadService = (VFSLoadService) this.ruby.getLoadService();
    }

    @Test
    public void testRubygemsLoadable() throws Exception {
        this.loadService.require( "rubygems" );
    }

}
