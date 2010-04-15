package org.torquebox.integration;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import static org.junit.Assert.*;

public class BasicTest {

	
	private HtmlUnitDriver driver;

	@Before
	public void setUpDriver() throws Exception {
		driver = new HtmlUnitDriver();
	}
	
	@Test
	public void testServerIsUp() throws Exception {
		driver.get( "http://localhost:8080/" );
		WebElement element = driver.findElementById( "success" );
		assertNotNull( element );
	}
}
