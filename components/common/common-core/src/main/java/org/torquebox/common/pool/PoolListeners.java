package org.torquebox.common.pool;

import java.util.ArrayList;
import java.util.List;

import org.torquebox.common.spi.PoolListener;

class PoolListeners<T> implements PoolListener<T> {
	
	private final List<PoolListener<T>> listeners = new ArrayList<PoolListener<T>>();
	
	public PoolListeners() {
	}
	
	List<PoolListener<T>> getListeners() {
		return this.listeners;
	}
	
	@Override
	public void instanceRequested(int totalInstances, int availableNow) {
		for ( PoolListener<T> each : this.listeners ) {
			each.instanceRequested( totalInstances, availableNow );
		}
	}

	@Override
	public void instanceBorrowed(T instance, int totalInstances, int availableNow) {
		for ( PoolListener<T> each : this.listeners ) {
			each.instanceBorrowed( instance, totalInstances, availableNow );
		}
	}

	@Override
	public void instanceReleased(T instance, int totalInstances, int availableNow) {
		for ( PoolListener<T> each : this.listeners ) {
			each.instanceReleased( instance, totalInstances, availableNow );
		}
		
	}

	@Override
	public void instanceDrained(T instance, int totalInstances, int availableNow) {
		for ( PoolListener<T> each : this.listeners ) {
			each.instanceDrained( instance, totalInstances, availableNow );
		}
	}

	@Override
	public void instanceFilled(T instance, int totalInstances, int availableNow) {
		for ( PoolListener<T> each : this.listeners ) {
			each.instanceFilled( instance, totalInstances, availableNow );
		}
		
	}


}
