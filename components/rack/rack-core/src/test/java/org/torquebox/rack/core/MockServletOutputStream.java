package org.torquebox.rack.core;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

public class MockServletOutputStream extends ServletOutputStream {

    private OutputStream out;

    public MockServletOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    public void write(int b) throws IOException {
        out.write( b );
    }

}
