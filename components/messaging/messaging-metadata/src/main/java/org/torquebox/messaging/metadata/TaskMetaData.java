package org.torquebox.messaging.metadata;

public class TaskMetaData {

    private String rubyClassName;
    private String location;

    public TaskMetaData() {

    }

    public void setRubyClassName(String rubyClassName) {
        this.rubyClassName = rubyClassName;
    }

    public String getRubyClassName() {
        return this.rubyClassName;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return this.location;
    }

}
