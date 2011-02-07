package org.torquebox.common.spi;

public interface ManageablePool<T> extends Pool<T> {
    void fillInstance(T instance);

    T drainInstance() throws Exception;

    T drainInstance(long timeout) throws Exception;

}
