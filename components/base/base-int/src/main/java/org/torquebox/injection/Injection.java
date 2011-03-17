package org.torquebox.injection;


public class Injection {
    
    private String siteName;
    private Injectable injectable;

    public Injection(String siteName, Injectable injectable) {
        this.siteName = siteName; 
        this.injectable = injectable;
    }
    
    public String getSiteName() {
        return this.siteName;
    }
    
    public Injectable getInjectable() {
        return this.injectable;
    }

}
