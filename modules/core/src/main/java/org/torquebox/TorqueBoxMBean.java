package org.torquebox;

import java.util.List;
import java.util.Map;

public interface TorqueBoxMBean {
    
    String getVersion();
    String getRevision();
    String getBuildNumber();
    String getBuildUser();
    List<String> getComponentNames();
    Map<String, String> getComponentBuildInfo(String componentName);
}
