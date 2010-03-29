package org.torquebox.mc;

import javax.naming.Context;
import javax.naming.NamingException;

import org.jboss.beans.metadata.plugins.AbstractDependencyValueMetaData;
import org.jboss.reflect.spi.TypeInfo;

public class JndiRefMetaData extends AbstractDependencyValueMetaData {

	private static final String DEPENDS_JNDI_PREFIX = "jndi:";

	private Context context;
	private String name;

	public JndiRefMetaData(Context context, String name) {
		this.context = context;
		this.name = name;
	}

	public Context getContext() {
		return this.context;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public Object getValue(TypeInfo info, ClassLoader cl) throws Throwable {
		return getValue();
	}

	@Override
	public Object getValue() {
		System.err.println("lookup JNDI [" + this.name + "]");
		try {
			Object value = this.context.lookup(this.name);
			System.err.println("found [" + value + "]");
			return value;
		} catch (NamingException e) {
			return null;
		}
	}

	@Override
	public Object getUnderlyingValue() {
		return DEPENDS_JNDI_PREFIX + getName();
	}

}