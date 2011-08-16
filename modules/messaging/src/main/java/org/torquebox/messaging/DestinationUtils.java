package org.torquebox.messaging;

public class DestinationUtils {
    
    public static String getServiceName(String destinationName) {
        if (destinationName.startsWith( "/" )) {
            destinationName = destinationName.substring( 1 );
        }
        return destinationName;
    }

}
