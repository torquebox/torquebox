package org.torquebox.common.spi;

public interface PoolListener<T> {

    void instanceRequested(int totalInstances, int availableNow);

    void instanceBorrowed(T instance, int totalInstances, int availableNow);

    void instanceReleased(T instance, int totalInstances, int availableNow);

    void instanceFilled(T instance, int totalInstances, int availableNow);

    void instanceDrained(T instance, int totalInstances, int availableNow);

}
