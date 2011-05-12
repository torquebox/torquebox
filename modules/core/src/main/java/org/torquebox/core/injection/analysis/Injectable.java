package org.torquebox.core.injection.analysis;


public interface Injectable {
    
    String getType();
    String getName();
    String getKey();
    boolean isGeneric();

}
