package org.torquebox.common.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class StringUtilsTest {

	@Test
	public void testCamelizeSimple() {
		assertEquals( "FooBar", StringUtils.camelize("foo_bar" ) );
	}
	
	@Test
	public void testCamelizeNested() {
		assertEquals( "FooBar::BazCheddar", StringUtils.camelize("foo_bar/baz_cheddar" ) );
	}
	
	@Test
	public void testCamelizeSimpleNoOp() {
		assertEquals( "FooBar", StringUtils.camelize( "FooBar" ) );
	}
	
	@Test
	public void testCamelizeNestedNoOp() {
		assertEquals( "FooBar::BazCheddar", StringUtils.camelize( "FooBar::BazCheddar" ) );
	}
	
	@Test
	public void testUnderscoreSimple() {
		assertEquals( "foo_bar", StringUtils.underscore( "FooBar" ) );
	}
	
	@Test
	public void testUnderscoreSimpleNested() {
		assertEquals( "foo_bar/baz_controller", StringUtils.underscore( "FooBar::BazController" ) );
	}
	
	@Test
	public void testUnderscoreSimpleNoOp() {
		assertEquals( "foo_bar", StringUtils.underscore( "foo_bar" ) );
	}
	
	@Test
	public void testUnderscoreNestedNoOp() {
		assertEquals( "foo_bar/baz_controller", StringUtils.underscore( "foo_bar/baz_controller" ) );
	}
	
}
