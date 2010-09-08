package org.torquebox.mc;

import javax.naming.Context;
import javax.naming.NamingException;

import org.jboss.beans.metadata.plugins.AbstractDependencyValueMetaData;
import org.jboss.beans.metadata.spi.MetaDataVisitor;
import org.jboss.reflect.spi.TypeInfo;
import org.jboss.util.JBossStringBuilder;

public class JndiRefMetaData extends AbstractDependencyValueMetaData {

	private static final long serialVersionUID = 1L;

	private static final String DEPENDS_JNDI_PREFIX = "naming:";

	private Context context;
	private String name;

	public JndiRefMetaData(Context context, String name) {
		log.info( "construct JNDI ref for " + context + " -> " + name );
		// System.err.println( "JndiRef Ctor" );
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
		log.info( "getValue() for " + this.name );
		return getValue();
	}

	@Override
	public Object getValue() {
		// System.err.println( "GETVALUE" );
		log.info( "getValue() for " + this.name );
		try {
			Object value = this.context.lookup(this.name);
			log.info( "value(" + this.name + ")=" + value );
			return value;
		} catch (NamingException e) {
			log.info( "naming exception: " + e );
			return null;
		}
	}

	@Override
	public Object getUnderlyingValue() {
		return DEPENDS_JNDI_PREFIX + getName();
	}

}