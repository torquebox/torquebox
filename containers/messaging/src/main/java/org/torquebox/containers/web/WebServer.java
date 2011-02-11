/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.torquebox.containers.web;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import org.jboss.logging.Logger;
import org.jboss.util.loading.ContextClassLoaderSwitcher;
import org.jboss.util.loading.ContextClassLoaderSwitcher.SwitchContext;

/**
 * A mini webserver that should be embedded in another application. It can
 * server any file that is available from classloaders that are registered with
 * it, including class-files.
 * 
 * Its primary purpose is to simplify dynamic class-loading in RMI. Create an
 * instance of it, register a classloader with your classes, start it, and
 * you'll be able to let RMI-clients dynamically download classes from it.
 * 
 * It is configured by calling any methods programmatically prior to startup.
 * 
 * @author <a href="mailto:marc@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:Scott.Stark@org.jboss">Scott Stark</a>
 * @authro <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision$
 * @see WebClassLoader
 */
public class WebServer implements Runnable {
    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------
    private static Logger log = Logger.getLogger( WebServer.class );

    /**
     * The interface to bind to. This is useful for multi-homed hosts that want
     * control over which interfaces accept connections.
     */
    private InetAddress bindAddress;

    /**
     * The serverSocket listen queue depth
     */
    private int backlog = 50;

    /**
     * The map of class loaders registered with the web server
     */
    private final ConcurrentHashMap loaderMap = new ConcurrentHashMap();

    /**
     * The web server http listening socket
     */
    private ServerSocket server = null;

    /**
     * A flag indicating if the server should attempt to download classes from
     * thread context class loader when a request arrives that does not have a
     * class loader key prefix.
     */
    private boolean downloadServerClasses = true;

    /**
     * A flag indicating if the server should attempt to download resources,
     * i.e. resource paths that don't end in .class
     */
    private boolean downloadResources = false;

    /**
     * The class wide mapping of type suffixes(class, txt) to their mime type
     * string used as the Content-Type header for the vended classes/resources
     */
    private static final Properties mimeTypes = new Properties();

    /**
     * The thread pool used to manage listening threads
     */
    private Executor executor;

    private final ContextClassLoaderSwitcher tclSwitcher;

    // Constructors --------------------------------------------------

    public WebServer() {
        @SuppressWarnings("unchecked")
        ContextClassLoaderSwitcher clSwitcher = (ContextClassLoaderSwitcher) AccessController.doPrivileged( ContextClassLoaderSwitcher.INSTANTIATOR );
        this.tclSwitcher = clSwitcher;
    }

    // Public --------------------------------------------------------

    /**
     * Get the http listening port
     * 
     * @return the http listening port
     */
    public int getPort() {
        return this.server.getLocalPort();
    }

    /**
     * Set the http server bind address
     * 
     * @param bindAddress
     */
    public void setBindAddress(InetAddress bindAddress) {
        this.bindAddress = bindAddress;

    }

    /**
     * Get the address the http server binds to
     * 
     * @return the http bind address
     */
    public InetAddress getBindAddress() {
        return bindAddress;
    }

    /**
     * Get the server sockets listen queue depth
     * 
     * @return the listen queue depth
     */
    public int getBacklog() {
        return backlog;
    }

    /**
     * Set the server sockets listen queue depth
     */
    public void setBacklog(int backlog) {
        if (backlog <= 0)
            backlog = 50;

        this.backlog = backlog;
    }

    public boolean getDownloadServerClasses() {
        return downloadServerClasses;
    }

    public void setDownloadServerClasses(boolean flag) {
        downloadServerClasses = flag;
    }

    public boolean getDownloadResources() {
        return downloadResources;
    }

    public void setDownloadResources(boolean flag) {
        downloadResources = flag;
    }

    public Executor getThreadPool() {
        return executor;
    }

    public void setThreadPool(Executor executor) {
        this.executor = executor;
    }

    public ServerSocket getServerSocket() {
        return this.server;
    }

    /**
     * Augment the type suffix to mime type mappings
     * 
     * @param extension
     *            - the type extension without a period(class, txt)
     * @param type
     *            - the mime type string
     */
    public void addMimeType(String extension, String type) {
        mimeTypes.put( extension, type );
    }

    /**
     * Start the web server on port and begin listening for requests.
     */
    public void start() throws Exception {
        if (executor == null)
            throw new IllegalArgumentException( "Required property 'executor' not specified" );

        try {
            server = new ServerSocket( 0, backlog, bindAddress );
            log.debug( "Started server: " + server );

            listen();
        } catch (java.net.BindException be) {
            throw new Exception( "Port already in use.", be );
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * Close the web server listening socket
     */
    public void stop() {
        try {
            ServerSocket srv = server;
            server = null;
            srv.close();
        } catch (Exception ignore) {
        }
    }

    /**
     * Add a class loader to the web server map and return the URL that should
     * be used as the annotated codebase for classes that are to be available
     * via RMI dynamic classloading. The codebase URL is formed by taking the
     * java.rmi.server.codebase system property and adding a subpath unique for
     * the class loader instance.
     * 
     * @param cl
     *            - the ClassLoader instance to begin serving download requests
     *            for
     * @return the annotated codebase to use if java.rmi.server.codebase is set,
     *         null otherwise.
     * @see #getClassLoaderKey(ClassLoader)
     */
    public URL addClassLoader(ClassLoader cl) {
        String key = getClassLoaderKey( cl );
        loaderMap.put( key, cl );
        URL loaderURL = null;
        String codebase = getCodebase();
        codebase += key;
        codebase += '/';
        try {
            loaderURL = new URL( codebase );
        } catch (MalformedURLException e) {
            log.error( "invalid url", e );
        }
        log.trace( "Added ClassLoader: " + cl + " URL: " + loaderURL );
        return loaderURL;
    }

    public String getCodebase() {
        InetAddress bindAddr = getBindAddress();

        try {
            if (bindAddr == null) {
                bindAddr = InetAddress.getLocalHost();
            }
            return "http://" + bindAddr.getHostAddress() + ":" + getPort() + "/";
        } catch (UnknownHostException e) {
            return "http://localhost:" + getPort() + "/";
        }
    }

    public void setJavaRmiServerCodebase() {
        log.info( "Setting java.rmi.server.code=" + getCodebase() );
        System.setProperty( "java.rmi.server.codebase", getCodebase() );
    }

    /**
     * Remove a class loader previously added via addClassLoader
     * 
     * @param cl
     *            - the ClassLoader previously added via addClassLoader
     */
    public void removeClassLoader(ClassLoader cl) {
        String key = getClassLoaderKey( cl );
        loaderMap.remove( key );
    }

    // Runnable implementation ---------------------------------------
    /**
     * Listen threads entry point. Here we accept a client connection and
     * located requested classes/resources using the class loader specified in
     * the http request.
     */
    public void run() {
        // Return if the server has been stopped
        if (server == null)
            return;

        // Accept a connection
        Socket socket = null;
        try {
            socket = server.accept();
        } catch (IOException e) {
            // If the server is not null meaning we were not stopped report the
            // err
            if (server != null)
                log.error( "Failed to accept connection", e );
            return;
        }

        // Create a new thread to accept the next connection
        listen();

        try {
            // Get the request socket output stream
            DataOutputStream out = new DataOutputStream( socket.getOutputStream() );
            try {
                String httpCode = "200 OK";
                // Get the requested item from the HTTP header
                BufferedReader in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
                String rawPath = getPath( in );

                // Parse the path into the class loader key and file path.
                //
                // The class loader key is a string whose format is
                // "ClassName[oid]", where the oid substring may contain '/'
                // chars. The expected form of the raw path is:
                //
                // "SomeClassName[some/object/id]/some/file/path"
                //
                // The class loader key is "SomeClassName[some/object/id]"
                // and the file path is "some/file/path"

                int endOfKey = rawPath.indexOf( ']' );
                String filePath = rawPath.substring( endOfKey + 2 );
                String loaderKey = rawPath.substring( 0, endOfKey + 1 );
                log.trace( "loaderKey = " + loaderKey );
                log.trace( "filePath = " + filePath );
                ClassLoader loader = (ClassLoader) loaderMap.get( loaderKey );
                /*
                 * If we did not find a class loader check to see if the raw
                 * path begins with className + '[' + class loader key + ']' by
                 * looking for an '[' char. If it does not and
                 * downloadServerClasses is true use the thread context class
                 * loader and set filePath to the rawPath
                 */
                if (loader == null && rawPath.indexOf( '[' ) < 0 && downloadServerClasses) {
                    filePath = rawPath;
                    log.trace( "No loader, reset filePath = " + filePath );
                    loader = Thread.currentThread().getContextClassLoader();
                }
                log.trace( "loader = " + loader );
                byte[] bytes = {};
                SwitchContext tclSwitchContext = null;
                try {
                    tclSwitchContext = this.tclSwitcher.getSwitchContext( this.getClass().getClassLoader() );
                    if (loader != null && filePath.endsWith( ".class" )) {
                        // A request for a class file
                        String className = filePath.substring( 0, filePath.length() - 6 ).replace( '/', '.' );
                        log.trace( "loading className = " + className );
                        Class<?> clazz = loader.loadClass( className );
                        URL clazzUrl = clazz.getProtectionDomain().getCodeSource().getLocation();
                        log.trace( "clazzUrl = " + clazzUrl );
                        if (clazzUrl == null) {
                            // Does the WebClassLoader of clazz
                            // have the ability to obtain the bytecodes of
                            // clazz?
                            /*
                             * bytes = ((WebClassLoader)
                             * clazz.getClassLoader()).getBytes(clazz); if
                             * (bytes == null) throw new
                             * Exception("Class not found: " + className);
                             */
                        } else {
                            if (clazzUrl.getFile().endsWith( "/" ) == false) {
                                clazzUrl = new URL( "jar:" + clazzUrl + "!/" + filePath );
                            }
                            // this is a hack for the AOP ClassProxyFactory
                            else if (clazzUrl.getFile().indexOf( "/org_jboss_aop_proxy$" ) < 0) {
                                clazzUrl = new URL( clazzUrl, filePath );
                            }

                            // Retrieve bytecodes
                            log.trace( "new clazzUrl: " + clazzUrl );
                            bytes = getBytes( clazzUrl );
                        }
                    } else if (loader != null && filePath.length() > 0 && downloadServerClasses && downloadResources) {
                        // Try getting resource
                        log.trace( "loading resource = " + filePath );
                        URL resourceURL = loader.getResource( filePath );
                        if (resourceURL == null)
                            httpCode = "404 Resource not found:" + filePath;
                        else {
                            // Retrieve bytes
                            log.trace( "resourceURL = " + resourceURL );
                            bytes = getBytes( resourceURL );
                        }
                    } else {
                        httpCode = "404 Not Found";
                    }
                } finally {
                    if (tclSwitchContext != null) {
                        tclSwitchContext.reset();
                    }
                }

                // Send bytecodes/resource data in response (assumes HTTP/1.0 or
                // later)
                try {
                    log.trace( "HTTP code=" + httpCode + ", Content-Length: " + bytes.length );
                    // The HTTP 1.0 header
                    out.writeBytes( "HTTP/1.0 " + httpCode + "\r\n" );
                    out.writeBytes( "Content-Length: " + bytes.length + "\r\n" );
                    out.writeBytes( "Content-Type: " + getMimeType( filePath ) );
                    out.writeBytes( "\r\n\r\n" );
                    // The response body
                    out.write( bytes );
                    out.flush();
                } catch (IOException ie) {
                    return;
                }
            } catch (Throwable e) {
                try {
                    log.trace( "HTTP code=404, " + e.getMessage() );
                    // Write out error response
                    out.writeBytes( "HTTP/1.0 404 Not Found\r\n" );
                    out.writeBytes( "Content-Type: text/html\r\n\r\n" );
                    out.flush();
                } catch (IOException ex) {
                    // Ignore
                }
            } finally {

            }
        } catch (IOException ex) {
            log.error( "error writting response", ex );
        } finally {
            // Close the client request socket
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    // Protected -----------------------------------------------------
    /**
     * Create the string key used as the key into the loaderMap.
     * 
     * @return The class loader instance key.
     */
    protected String getClassLoaderKey(ClassLoader cl) {
        String className = cl.getClass().getName();
        int dot = className.lastIndexOf( '.' );
        if (dot >= 0)
            className = className.substring( dot + 1 );
        String key = className + '[' + cl.hashCode() + ']';
        return key;
    }

    protected void listen() {
        executor.execute( this );
    }

    /**
     * @return the path portion of the HTTP request header.
     */
    protected String getPath(BufferedReader in) throws IOException {
        String line = in.readLine();
        log.trace( "raw request=" + line );
        // Find the request path by parsing the 'REQUEST_TYPE filePath
        // HTTP_VERSION' string
        int start = line.indexOf( ' ' ) + 1;
        int end = line.indexOf( ' ', start + 1 );
        // The file minus the leading '/'
        String filePath = line.substring( start + 1, end );
        return filePath;
    }

    /**
     * Read the local class/resource contents into a byte array.
     */
    protected byte[] getBytes(URL url) throws IOException {
        InputStream in = new BufferedInputStream( url.openStream() );
        log.debug( "Retrieving " + url );
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] tmp = new byte[1024];
        int bytes;
        while ((bytes = in.read( tmp )) != -1) {
            out.write( tmp, 0, bytes );
        }
        in.close();
        return out.toByteArray();
    }

    /**
     * Lookup the mime type for the suffix of the path argument.
     * 
     * @return the mime-type string for path.
     */
    protected String getMimeType(String path) {
        int dot = path.lastIndexOf( "." );
        String type = "text/html";
        if (dot >= 0) {
            // The suffix is the type extension without the '.'
            String suffix = path.substring( dot + 1 );
            String mimeType = mimeTypes.getProperty( suffix );
            if (mimeType != null)
                type = mimeType;
        }
        return type;
    }

}
