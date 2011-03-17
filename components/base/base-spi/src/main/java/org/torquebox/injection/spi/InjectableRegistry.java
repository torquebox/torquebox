package org.torquebox.injection.spi;

public interface InjectableRegistry {
    Object get(String collectionName, String objectName);

}
