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

package org.torquebox.integration;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.jvnet.winp.WinProcess;
import org.junit.Test;
import org.torquebox.test.AbstractTorqueBoxTestCase;

public class CommandLineMessagingTest extends AbstractTorqueBoxTestCase {

    @Test
    public void testQueuing() throws Exception {
        Process broker = null, hoster = null;
        try {
            broker = start( jruby(), jrubyBin( "trq-message-broker" ), "-s", "-d", "queues.yml" );
            assertTrue( "Broker failed to start", lookFor( "deployed queues.yml", broker.getInputStream() ) );
            hoster = start( jruby(), jrubyBin( "trq-message-processor-host" ), "-d", "messaging.yml" );
            assertTrue( "Processor host failed to start", lookFor( "deployed messaging.yml", hoster.getInputStream() ) );
            start( jruby(), "messenger.rb", "/queues/foo", "did it work?" );
            assertTrue( "Didn't receive expected message", lookFor( "received: did it work?", hoster.getInputStream() ) );
        } finally {
            if (hoster != null) {
                stop(hoster);
            }
            if (broker != null) {
                stop(broker);
            }
        }
    }

    private boolean lookFor(final String target, final InputStream input) throws Exception {
        return new ProcessOutputSearcher( target, input ).search( 40000 );
    }

    private String jrubyBin(String script) throws Exception {
        String home = System.getProperty( "jruby.home" );
        if (home == null) {
            throw new RuntimeException( "You must set system property, jruby.home" );
        }
        return new File( new File( home, "bin" ), script ).getCanonicalPath();
    }

    private String jruby() throws Exception {
        String jruby = jrubyBin( "jruby" );

        if (isWindows()) {
            jruby = jruby + ".exe";
        }

        return jruby;
    }

    private Process start(String... args) throws Exception {
        return new ProcessBuilder( args ).redirectErrorStream( true ).directory( new File( System.getProperty( "user.dir" ), "src/test/resources/messaging" ) ).start();
    }

    private void stop(Process process) throws Exception {
        if (isWindows()) {
            new WinProcess( process ).killRecursively();
        } else {
            process.destroy();
        }
    }

    class ProcessOutputSearcher implements Runnable {

        ProcessOutputSearcher(String target, InputStream input) {
            this.target = target;
            this.input = input;
        }

        boolean search(long timeout) throws InterruptedException {
            Thread t = new Thread( this );
            t.start();
            t.join( timeout );
            return this.found;
        }

        public void run() {
            BufferedReader in = new BufferedReader( new InputStreamReader( input ) );
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println( line );
                    if (line.contains( target )) {
                        this.found = true;
                        break;
                    }
                }
            } catch (Exception ignored) {
            }

        }

        private String target;
        private InputStream input;
        private boolean found;
    }
}
