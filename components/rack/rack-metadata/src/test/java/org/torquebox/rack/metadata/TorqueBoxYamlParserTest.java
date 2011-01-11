package org.torquebox.rack.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.vfs.VFS;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.test.AbstractTorqueBoxTestCase;


public class TorqueBoxYamlParserTest extends AbstractTorqueBoxTestCase {
    
    private TorqueBoxYamlParser parser;
    private RackApplicationMetaData metadata;
    private Map<String,String> strings;
    private Map<String,Object> objects;

    @Before
    public void setUp() throws Exception {
        metadata = new RackApplicationMetaData();
        parser = new TorqueBoxYamlParser(metadata);
        strings = new HashMap<String,String>();
        objects = new HashMap<String,Object>();
    }
    
    @Test
    public void testEmptyFile() throws Exception {
        assertNull( parser.parse( VFS.getChild("") ) );
        assertEquals( metadata, parser.parse( objects ) );
    }

    @Test
    public void testEmptyWeb() throws Exception {
        assertEquals( metadata, parser.parseWeb(null) );
        parser.parseWeb(objects);
        assertEquals( Collections.EMPTY_LIST, metadata.getHosts() );
    }
        
    @Test
    public void testEmptyEnvironment() throws Exception {
        assertEquals( metadata, parser.parseEnvironment(null) );
    }

    @Test
    public void testEmptyApplication() throws Exception {
        assertEquals( metadata, parser.parseApplication(null) );
        assertEquals( metadata, parser.parseApplication(strings) );
    }

    @Test 
    public void testAbsoluteRackUpScript() throws Exception {
        strings.put("rackup", pwd() +  "/src/test/resources/config.ru");
        metadata = parser.parseApplication(strings);
        assertEquals("success!\n", metadata.getRackUpScript());
    }

    @Test 
    public void testRelativeRackUpScript() throws Exception {
        strings.put("RACK_ROOT", pwd() );
        strings.put("rackup", "src/test/resources/config.ru");
        metadata = parser.parseApplication(strings);
        System.err.println( metadata );
        assertEquals("success!\n", metadata.getRackUpScript());
    }

    @Test 
    public void testLenientRootKeys() throws Exception {
        String root = null;
        String expectedRoot = null;
        
        if ( isWindows() ) {
        	root = "C:/test";
        	expectedRoot = "/" + root;
        } else {
        	root = "/test";
        	expectedRoot = root;
        }
        
        strings.put("RACK_ROOT", root);
        assertEquals( expectedRoot, parser.parseApplication(strings).getRackRoot().getPathName() );
        
        setUp(); strings.put("RAILS_ROOT", root);
        assertEquals( expectedRoot, parser.parseApplication(strings).getRackRoot().getPathName() );
        
        setUp(); strings.put("rack_root", root);
        assertEquals( expectedRoot, parser.parseApplication(strings).getRackRoot().getPathName() );
        
        setUp(); strings.put("rails_root", root);
        assertEquals( expectedRoot, parser.parseApplication(strings).getRackRoot().getPathName() );
        
        setUp(); strings.put("root", root);
        assertEquals( expectedRoot, parser.parseApplication(strings).getRackRoot().getPathName() );
        
        setUp(); strings.put("ROOT", root);
        assertEquals( expectedRoot, parser.parseApplication(strings).getRackRoot().getPathName() );
        
        setUp(); strings.put("RaCk_RoOt", root);
        assertEquals( expectedRoot, parser.parseApplication(strings).getRackRoot().getPathName() );
    }

    @Test 
    public void testLenientEnvKeys() throws Exception {
        String env = "development";
        strings.put("RACK_ENV", env);
        assertEquals( env, parser.parseApplication(strings).getRackEnv() );
        setUp(); strings.put("RAILS_ENV", env);
        assertEquals( env, parser.parseApplication(strings).getRackEnv() );
        setUp(); strings.put("rack_env", env);
        assertEquals( env, parser.parseApplication(strings).getRackEnv() );
        setUp(); strings.put("rails_env", env);
        assertEquals( env, parser.parseApplication(strings).getRackEnv() );
        setUp(); strings.put("env", env);
        assertEquals( env, parser.parseApplication(strings).getRackEnv() );
        setUp(); strings.put("ENV", env);
        assertEquals( env, parser.parseApplication(strings).getRackEnv() );
        setUp(); strings.put("RaCk_EnV", env);
        assertEquals( env, parser.parseApplication(strings).getRackEnv() );
    }
}
