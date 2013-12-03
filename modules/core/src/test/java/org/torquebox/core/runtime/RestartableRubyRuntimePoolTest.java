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

package org.torquebox.core.runtime;

import org.jruby.Ruby;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class RestartableRubyRuntimePoolTest {

    @Test
    public void testFoo() throws Exception {
        DefaultRubyRuntimePool pool = new DefaultRubyRuntimePool(new RubyRuntimeFactory());
        pool.setMinimumInstances(5);
        pool.setMaximumInstances(5);
        pool.setDeferUntilRequested(false);
        final RestartableRubyRuntimePool restartablePool = new RestartableRubyRuntimePool(pool);
        restartablePool.start();

        List<Thread> threads = new ArrayList<Thread>();
        for (int i = 0; i < 25; i++) {
            threads.add(new Thread(new Runnable() {
                public void run() {
                    try {
                        Ruby runtime = restartablePool.borrowRuntime("me");
                        Thread.sleep(10L);
                        restartablePool.returnRuntime(runtime);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }));
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        restartablePool.stop();
    }
}
