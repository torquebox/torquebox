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

package org.torquebox.core.pool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class SimplePoolTest extends AbstractPoolTestCase {

    @Test
    public void testEmptyPool() throws Exception {
        SimplePool<String> pool = new SimplePool<String>();
        assertBorrowTimeout( pool );
    }

    @Test
    public void testFillBorrowReleaseDrain() throws Exception {

        SimplePool<String> pool = new SimplePool<String>();
        assertBorrowTimeout( pool );

        pool.fillInstance( "Instance-1" );
        pool.fillInstance( "Instance-2" );
        pool.fillInstance( "Instance-3" );

        Set<String> instances = new HashSet<String>();

        instances.add( assertBorrow( pool ) );
        instances.add( assertBorrow( pool ) );
        instances.add( assertBorrow( pool ) );

        assertTrue( instances.contains( "Instance-1" ) );
        assertTrue( instances.contains( "Instance-2" ) );
        assertTrue( instances.contains( "Instance-3" ) );

        assertBorrowTimeout( pool );

        pool.releaseInstance( "Instance-2" );

        assertEquals( "Instance-2", assertBorrow( pool ) );

        pool.releaseInstance( "Instance-1" );
        pool.releaseInstance( "Instance-2" );
        pool.releaseInstance( "Instance-3" );

        instances.clear();

        instances.add( assertDrain( pool ) );
        instances.add( assertDrain( pool ) );
        instances.add( assertDrain( pool ) );

        assertTrue( instances.contains( "Instance-1" ) );
        assertTrue( instances.contains( "Instance-2" ) );
        assertTrue( instances.contains( "Instance-3" ) );

        assertBorrowTimeout( pool );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testListenerCallbacks() throws Exception {
        SimplePool<String> pool = new SimplePool<String>();

        PoolListener<String> listener = mock( PoolListener.class );
        pool.addListener( listener );

        pool.fillInstance( "Instance-1" );

        assertBorrow( pool );

        verify( listener ).instanceBorrowed( "Instance-1", 1, 0 );

        pool.releaseInstance( "Instance-1" );

        verify( listener ).instanceReleased( "Instance-1", 1, 1 );

        pool.requestInstance();

        verify( listener ).instanceRequested( 1, 1 );
    }

}
