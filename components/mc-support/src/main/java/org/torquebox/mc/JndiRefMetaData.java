package org.torquebox.mc;

import javax.naming.Context;
import javax.naming.NamingException;

import org.jboss.beans.metadata.plugins.AbstractDependencyValueMetaData;
import org.jboss.reflect.spi.TypeInfo;

public class JndiRefMetaData extends AbstractDependencyValueMetaData {

	private static final long serialVersionUID = 1L;

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
		try {
			return this.context.lookup(this.name);
		} catch (NamingException e) {
			return null;
		}
	}

	@Override
	public Object getUnderlyingValue() {
		return DEPENDS_JNDI_PREFIX + getName();
	}

}