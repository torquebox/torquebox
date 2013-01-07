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

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class ManagedPoolTest extends AbstractPoolTestCase {

    private StringInstanceFactory factory;

    @Before
    public void setUp() throws Exception {
        this.factory = new StringInstanceFactory();
    }

    @Test
    public void testDeferredPoolShouldDefer() throws Exception {
        ManagedPool<String> pool = new ManagedPool<String>( this.factory, 5, 10 );
        pool.start();
        assertEquals( false, pool.isStarted() );
    }
    
    @Test
    public void testNonDeferredPoolShouldNotDefer() throws Exception {
        ManagedPool<String> pool = new ManagedPool<String>( this.factory, 5, 10 );
        pool.setDeferUntilRequested( false );
        pool.start();
        assertEquals( true, pool.isStarted() );
        pool.stop();
    }

    @Test
    public void testInitializeMinimumInstances() throws Exception {
        ManagedPool<String> pool = new ManagedPool<String>( this.factory, 5, 10 );
        pool.startPool();

        while (pool.size() < 5) {
            Thread.sleep( 500 );
        }

        Thread.sleep( 1000 );

        assertEquals( 5, pool.size() );
        assertEquals( 5, pool.availableSize() );
        assertEquals( 0, pool.borrowedSize() );
        
        pool.stop();
        assertEquals( 0, pool.size() );
    }

    @Test
    public void testGrowWithinBounds() throws Exception {
        ManagedPool<String> pool = new ManagedPool<String>( this.factory, 5, 10 );
        pool.startPool();
        pool.waitForInitialFill();

        Set<String> instances = new HashSet<String>();

        instances.add( assertBorrow( pool ) );
        instances.add( assertBorrow( pool ) );
        instances.add( assertBorrow( pool ) );
        instances.add( assertBorrow( pool ) );
        instances.add( assertBorrow( pool ) );

        assertEquals( 5, pool.size() );
        assertEquals( 5, pool.borrowedSize() );
        assertEquals( 0, pool.availableSize() );

        instances.add( assertBorrow( pool ) );
        instances.add( assertBorrow( pool ) );
        instances.add( assertBorrow( pool ) );

        assertEquals( 8, pool.size() );
        assertEquals( 8, pool.borrowedSize() );
        assertEquals( 0, pool.availableSize() );

        for (String each : instances) {
            pool.releaseInstance( each );
        }

        assertEquals( 8, pool.size() );
        assertEquals( 0, pool.borrowedSize() );
        assertEquals( 8, pool.availableSize() );
        
        pool.stop();
        assertEquals( 0, pool.size() );
    }

    @Test
    public void testGrowToBounds() throws Exception {
        growToBounds( new ManagedPool<String>( this.factory, 5, 10 ) );
    }

    @Test
    public void testGrowToBoundsWithBeanInit() throws Exception {
        ManagedPool<String> pool = new ManagedPool<String>( this.factory );
        pool.setMinimumInstances( 5 );
        pool.setMaximumInstances( 10 );
        growToBounds( pool );
    }

    private void growToBounds(ManagedPool<String> pool) throws Exception {
        pool.startPool();
        pool.waitForInitialFill();

        Set<String> instances = new HashSet<String>();

        instances.add( assertBorrow( pool ) );
        instances.add( assertBorrow( pool ) );
        instances.add( assertBorrow( pool ) );
        instances.add( assertBorrow( pool ) );
        instances.add( assertBorrow( pool ) );

        assertEquals( 5, pool.size() );
        assertEquals( 5, pool.borrowedSize() );
        assertEquals( 0, pool.availableSize() );

        instances.add( assertBorrow( pool ) );
        instances.add( assertBorrow( pool ) );
        instances.add( assertBorrow( pool ) );
        instances.add( assertBorrow( pool ) );
        instances.add( assertBorrow( pool ) );

        assertEquals( 10, pool.size() );
        assertEquals( 10, pool.borrowedSize() );
        assertEquals( 0, pool.availableSize() );

        assertBorrowTimeout( pool );

        int available = 0;
        for (String each : instances) {
            assertEquals( available, pool.availableSize() );
            pool.releaseInstance( each );
            ++available;
            assertEquals( available, pool.availableSize() );
        }

        assertEquals( 10, pool.size() );
        assertEquals( 0, pool.borrowedSize() );
        assertEquals( 10, pool.availableSize() );
        
        pool.stop();
        assertEquals( 0, pool.size() );
    }

    /*
    @Test
    public void testGrowToBoundsLoop() throws Exception {
        for (int i = 0; i < 40; ++i) {
            testGrowToBounds();
        }

    }
    */

}
