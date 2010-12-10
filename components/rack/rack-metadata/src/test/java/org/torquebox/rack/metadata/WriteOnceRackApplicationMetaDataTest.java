package org.torquebox.rack.metadata;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;


public class WriteOnceRackApplicationMetaDataTest {
    
    private RackApplicationMetaData metadata;

    @Before
    public void setUp() throws Throwable {
        metadata = new WriteOnceRackApplicationMetaData();
    }
    
    @Test
    public void testWritingRackEnv() throws Exception {
        metadata.setRackEnv("development");
        metadata.setRackEnv("production");
        assertEquals("development", metadata.getRackEnv());
    }

    @Test
    public void testWritingEnvironmentVariables() throws Exception {
        Map<String,String> env = new HashMap<String,String>();
        env.put("foo", "bar");
        env.put("boo", "far");
        metadata.setEnvironmentVariables(env);
        assertEquals("bar", metadata.getEnvironmentVariables().get("foo"));
        assertEquals("far", metadata.getEnvironmentVariables().get("boo"));

        env.put("foo", "xxx");  // replace
        env.remove("boo");      // delete
        env.put("bar", "foo");  // add
        metadata.setEnvironmentVariables(env);
        assertEquals("bar", metadata.getEnvironmentVariables().get("foo"));
        assertEquals("far", metadata.getEnvironmentVariables().get("boo"));
        assertEquals("foo", metadata.getEnvironmentVariables().get("bar"));
    }
}
