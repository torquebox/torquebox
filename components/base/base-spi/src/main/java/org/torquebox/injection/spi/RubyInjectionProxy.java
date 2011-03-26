package org.torquebox.injection.spi;

import java.util.Map;

public interface RubyInjectionProxy {
    
    void setInjectionRegistry(Map<String,Object> injectionRegistry);

}
