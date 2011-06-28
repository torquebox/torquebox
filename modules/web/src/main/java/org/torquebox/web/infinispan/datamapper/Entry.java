package org.torquebox.web.infinispan.datamapper;

import java.io.Serializable;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.ProvidedId;

@Indexed
@ProvidedId
public class Entry implements Serializable {
	private static final long serialVersionUID = -3714284008075882375L;

	@Field
	String model;
	@Field
	String data;
	@Field
	String key;

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
