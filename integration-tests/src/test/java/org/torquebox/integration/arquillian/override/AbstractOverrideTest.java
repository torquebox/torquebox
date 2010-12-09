package org.torquebox.integration.arquillian.override;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.torquebox.integration.arquillian.AbstractIntegrationTest;


public abstract class AbstractOverrideTest extends AbstractIntegrationTest {
    
    protected String context;
    protected String app;
    protected String home;
    protected String env;

	@Test
	public void testStaticContent() {
		driver.get("http://localhost:8080/" + context + "/index.html");
		WebElement element = driver.findElementById("success");
		assertNotNull(element);
		assertEquals(app, element.getAttribute("class"));
	}

	@Test
	@Ignore
	public void testRackRoot() {
        assertEquals("vfs:" + System.getProperty("basedir") + home, getEnvironmentVariable("RACK_ROOT"));
	}

	@Test
	public void testRackEnv() {
        assertEquals(env, getEnvironmentVariable("RACK_ENV"));
	}

	@Test
	public void testEnvironmentVariables() {
        assertEquals(app, getEnvironmentVariable("APP"));
        assertEquals(app+" foo", getEnvironmentVariable("foo"));
        assertEquals(app+" bar", getEnvironmentVariable("bar"));
	}

    protected String getEnvironmentVariable(String name) {
		driver.get("http://localhost:8080/" + context + "/" +name);
        WebElement body = driver.findElementByTagName("body");
        return body.getText().trim();
    }
}
