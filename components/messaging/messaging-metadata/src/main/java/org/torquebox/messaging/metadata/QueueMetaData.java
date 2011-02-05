package org.torquebox.messaging.metadata;

public class QueueMetaData extends AbstractDestinationMetaData {

    public QueueMetaData() {

    }

    public QueueMetaData(String name) {
        super( name );
    }

    public String toString() {
        return "[QueueMetaData: name=" + getName() + "]";
    }

}
