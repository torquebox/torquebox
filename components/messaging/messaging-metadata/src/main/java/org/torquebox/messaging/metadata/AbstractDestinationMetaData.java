package org.torquebox.messaging.metadata;

public class AbstractDestinationMetaData {

    private String name;
    private String bindName;
    private boolean durable;

    public AbstractDestinationMetaData() {

    }

    public AbstractDestinationMetaData(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBindName() {
        return this.bindName;
    }

    public void setBindName(String bindName) {
        this.bindName = bindName;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    public boolean isDurable() {
        return this.durable;
    }
}
