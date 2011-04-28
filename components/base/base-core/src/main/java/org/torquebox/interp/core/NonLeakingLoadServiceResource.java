package org.torquebox.interp.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.jruby.runtime.load.LoadServiceResource;
import org.jruby.runtime.load.LoadServiceResourceInputStream;

public class NonLeakingLoadServiceResource extends LoadServiceResource {

    private URL resource;

    public NonLeakingLoadServiceResource(URL resource, String name) {
        super( resource, name );
        this.resource = resource;
    }

    public NonLeakingLoadServiceResource(URL resource, String name, boolean absolute) {
        super( resource, name, absolute );
        this.resource = resource;
    }

    public InputStream getInputStream() throws IOException {
        if (resource != null) {
            InputStream resourceStream = resource.openStream();
            try {
                return new LoadServiceResourceInputStream( resourceStream );
            } finally {
                if (resourceStream != null) {
                    resourceStream.close();
                }
            }
        }
        
        return super.getInputStream();
    }

}
