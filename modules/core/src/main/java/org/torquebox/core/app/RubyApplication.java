package org.torquebox.core.app;

import java.util.Date;

public class RubyApplication implements RubyApplicationMBean {
    
    private String name;
    private String rootPath;
    private Date deployedAt;
    private String environmentName;

    public RubyApplication() {
        this.deployedAt = new Date();
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }
    
    public String getRootPath() {
        return this.rootPath;
    }
    
    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }
    
    public String getEnvironmentName() {
        return this.environmentName;
    }
    
    public Date getDeployedAt() {
        return this.deployedAt;
    }

}
