package org.torquebox.integration.arquillian;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.torquebox.integration.FancyHtmlUnitDriver;
import org.torquebox.test.AbstractTorqueBoxTestCase;

@RunWith(Arquillian.class)
public abstract class AbstractIntegrationTest  extends AbstractTorqueBoxTestCase {

    protected FancyHtmlUnitDriver driver;

    @Before
    public void setUp() {
        this.driver = new FancyHtmlUnitDriver();
    }

    @After
    public void tearDown() {
        this.driver = null;
    }

    public HtmlUnitDriver getDriver() {
        return this.driver;
    }

    public static JavaArchive createDeployment(String name) {
        int lastSlashLoc = name.lastIndexOf('/');
        String tail = name.substring(lastSlashLoc + 1);
        int lastDot = tail.lastIndexOf('.');
        String base = tail.substring(0, lastDot);

        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, base + ".jar");
        ClassLoader classLoader = AbstractIntegrationTest.class.getClassLoader();
        URL deploymentDescriptorUrl = classLoader.getResource(name);
        archive.addResource(deploymentDescriptorUrl, "/META-INF/" + tail);
        return archive;
    }

    protected String slurpResource(String path) throws IOException {
        System.err.println("attempting to slurp: " + path);
        InputStream in = getClass().getClassLoader().getResourceAsStream(path);
        System.err.println("  in=" + in);
        try {
            return slurp(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    protected String slurp(InputStream in) throws IOException {
        StringBuilder builder = new StringBuilder();
        Reader reader = new InputStreamReader(in);
        char[] buf = new char[1024];

        int numRead = 0;

        while ((numRead = reader.read(buf)) >= 0) {
            builder.append(buf, 0, numRead);
        }

        return builder.toString();
    }

    protected void rm(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                for (File child : file.listFiles()) {
                    rm(child);
                }
            }
            if (!file.delete()) {
                throw new IOException("Unable to delete: " + file.getAbsolutePath());
            }
        }

    }

}
