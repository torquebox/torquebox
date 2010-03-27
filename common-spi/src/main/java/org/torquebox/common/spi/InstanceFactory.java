package org.torquebox.common.spi;

import org.jboss.beans.metadata.api.annotations.Create;

public interface InstanceFactory<T> {
	
	@Create(ignored=true)
	T create() throws Exception;
	

}
