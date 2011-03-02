package org.torquebox;

import java.util.Date;

public interface RubyApplicationMBean {
    
    String getName();
    String getRootPath();
    String getEnvironmentName();
    Date getDeployedAt();

}
