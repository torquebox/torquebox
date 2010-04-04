package org.torquebox.rack.core;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;

public class MockServletInputStream extends ServletInputStream {

	
	private InputStream in;

	public MockServletInputStream(InputStream in) {
		this.in = in;
	}
	
	@Override
	public int read() throws IOException {
		return in.read();
	}

}
