package org.torquebox.integration.arquillian.alacarte;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.integration.arquillian.AbstractIntegrationTestCase;

@Run(RunModeType.AS_CLIENT)
public class JobsTest extends AbstractIntegrationTestCase {

    @Deployment
    public static JavaArchive createDeployment() {
        return createDeployment( "alacarte/jobs-knob.yml" );
    }

    private File file;

    @Before
    public void setUp() {
        this.file = new File( System.getProperty( "basedir" ) + "/target/touchfile.txt" );
        this.file.delete();
    }

    @Test
    public void testDeployment() throws Exception {
        Set<String> seenValues = new HashSet<String>();

        for (int i = 0; i < 10; ++i) {
            Thread.sleep( 1000 );
            if (this.file.exists()) {
                BufferedReader reader = new BufferedReader( new FileReader( this.file ) );

                StringBuffer buffer = new StringBuffer();
                String line = null;

                while ((line = reader.readLine()) != null) {
                    buffer.append( line.trim() );
                }
                String value = buffer.toString().trim();
                
                seenValues.add(  value  );
            }
        }
        
        assertFalse( seenValues.isEmpty() );
        assertTrue( seenValues.size() > 5 );
    }

}
