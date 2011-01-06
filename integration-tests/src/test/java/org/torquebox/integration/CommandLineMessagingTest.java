package org.torquebox.integration;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;
import org.torquebox.test.AbstractTorqueBoxTestCase;


public class CommandLineMessagingTest extends AbstractTorqueBoxTestCase {

	@Test
    public void testQueuing() throws Exception {
        Process broker=null, hoster=null;
        try {
            broker = start( jruby(), jrubyBin("trq-message-broker"), "-s", "-d", "queues.yml");
            assertTrue( "Broker failed to start", lookFor( "deployed queues.yml", broker.getInputStream() ) );
            hoster = start( jruby(), jrubyBin("trq-message-processor-host"), "-d", "messaging.yml");
            assertTrue( "Processor host failed to start", lookFor( "starting for", hoster.getInputStream() ) );
            start( jruby(), "messenger.rb", "/queues/foo", "did it work?" );
            assertTrue( "Didn't receive expected message", lookFor( "received: did it work?", hoster.getInputStream() ) );
        } finally {
            if (hoster != null) hoster.destroy();
            if (broker != null) broker.destroy();
        }
	}

    private boolean lookFor(final String target, final InputStream input) throws Exception {
        return new ProcessOutputSearcher(target, input).search(40000);
    }

    private String jrubyBin(String script) throws Exception {
        String home = System.getProperty("jruby.home");
        if (home==null) throw new RuntimeException("You must set system property, jruby.home");
        return new File(new File(home, "bin"), script).getCanonicalPath();
    }
    
    private String jruby() throws Exception {
    	String jruby = jrubyBin( "jruby" );
    	
    	if ( isWindows() ) {
    		jruby = jruby + ".exe";
    	}
    	
    	return jruby;
    }

    private Process start(String... args) throws Exception {
        return new ProcessBuilder(args)
            .redirectErrorStream(true)
            .directory( new File( System.getProperty("user.dir"), "src/test/resources/messaging" ) )
            .start();
    }

    class ProcessOutputSearcher implements Runnable {

        ProcessOutputSearcher(String target, InputStream input) {
            this.target = target;
            this.input = input;
        }
    
        boolean search(long timeout) throws InterruptedException {
            Thread t = new Thread(this);
            t.start();
            t.join(timeout);
            return this.found;
        }

        public void run() {
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            try {
                String line;
                while ((line=in.readLine()) != null) {
                    System.out.println(line);
                    if (line.contains(target)) {
                        this.found = true;
                        break;
                    }
                }
            } catch (Exception ignored) {}
        
        }

        private String target;
        private InputStream input;
        private boolean found;
    }
}

