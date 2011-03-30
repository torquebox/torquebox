package org.torquebox.base.metadata;

import static org.junit.Assert.*;
import org.junit.*;
import java.util.*;


public class TorqueBoxMetaDataTest {

    @Test
    public void testOverlayingWebSection() {
        Map<String,String> external = new HashMap<String,String>();
        external.put("context", "/");
        Map<String,String> internal = new HashMap<String,String>();
        internal.put("rackup", "foo.ru");
        
        Map<String,Object> externalWeb = new HashMap<String,Object>();
        externalWeb.put("web", external);
        Map<String,Object> internalWeb = new HashMap<String,Object>();
        internalWeb.put("web", internal);

        TorqueBoxMetaData externalMetaData = new TorqueBoxMetaData(externalWeb);
        TorqueBoxMetaData internalMetaData = new TorqueBoxMetaData(internalWeb);
        TorqueBoxMetaData mergedMetaData = externalMetaData.overlayOnto(internalMetaData);

        Map<String,String> merged = (Map<String,String>) mergedMetaData.getSection("web");
        assertEquals("/", merged.get("context"));
        assertEquals("foo.ru", merged.get("rackup"));
    }
    
}
