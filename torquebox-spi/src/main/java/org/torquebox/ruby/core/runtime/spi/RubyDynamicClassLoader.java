package org.torquebox.ruby.core.runtime.spi;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public interface RubyDynamicClassLoader {

	public abstract void addLoadPaths(List<String> paths)
			throws URISyntaxException, IOException;

}