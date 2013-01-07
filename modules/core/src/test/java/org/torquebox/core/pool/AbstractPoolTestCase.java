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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public abstract class AbstractPoolTestCase {

    private long TIMEOUT = 500;

    protected <T> T assertBorrow(Pool<T> pool) throws Exception {
        T instance = pool.borrowInstance( "test" );
        assertNotNull( instance );
        return instance;
    }

    protected <T> T assertDrain(ManageablePool<T> pool) throws Exception {
        T instance = pool.drainInstance( TIMEOUT );
        assertNotNull( instance );
        return instance;
    }

    protected <T> void assertBorrowTimeout(Pool<T> pool) throws Exception {
        long start = System.currentTimeMillis();
        Object instance = pool.borrowInstance( "test", TIMEOUT );
        long stop = System.currentTimeMillis();
        long elapsed = stop - start;
        assertNull( instance );
        assertTrue( elapsed > (TIMEOUT - 1000) );
        assertTrue( elapsed < (TIMEOUT + 1000) );
    }

}
