/*
 * Copyright 2008-2016 Red Hat, Inc, and individual contributors.
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

package org.torquebox.web.rails;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RailsMetaDataTest {

    @Test
    public void testRailsVersionRegex() {
        RailsMetaData metaData = new RailsMetaData();
        metaData.setVersionSpec("2.3.14");
        assertTrue(metaData.isRails2());
        assertFalse(metaData.isRails3());

        metaData.setVersionSpec("~>3.2.1");
        assertFalse(metaData.isRails2());
        assertTrue(metaData.isRails3());

        metaData.setVersionSpec("~> 3.2.1");
        assertFalse(metaData.isRails2());
        assertTrue(metaData.isRails3());

        metaData.setVersionSpec("3.2.22.1");
        assertFalse(metaData.isRails2());
        assertTrue(metaData.isRails3());

        metaData.setVersionSpec("4.2.7");
        assertFalse(metaData.isRails2());
        assertTrue(metaData.isRails3());

        metaData.setVersionSpec("5.0.1");
        assertFalse(metaData.isRails2());
        assertTrue(metaData.isRails3());
    }
}
