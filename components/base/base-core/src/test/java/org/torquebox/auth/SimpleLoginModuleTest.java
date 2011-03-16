package org.torquebox.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

public class SimpleLoginModuleTest {
    
    private SimpleLoginModule module = new SimpleLoginModule();
    private Map<String,Map<String,String>> options    = new HashMap<String, Map<String, String>>();
    
    @Before
    public void setUp() {
        Map<String,String> users = new HashMap<String,String>();
        users.put("scott", "tiger");
        options.put("users", users);
        Map<String,String> roles = new HashMap<String,String>();
        roles.put("admin", "scott");
        options.put("roles", roles);
    }
    
    @Test
    public void testCreateRoles() {
        Properties roles = module.createRoles(options);
        assertNotNull(roles);
        assertEquals("scott", roles.get("admin"));
    }
    
    @Test
    public void testCreateUsers() {
        Properties users = module.createUsers(options);
        assertNotNull(users);
        assertEquals("tiger", users.get("scott"));
    }
}
