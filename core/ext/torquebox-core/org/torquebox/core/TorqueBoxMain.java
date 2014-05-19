package org.torquebox.core;

import org.projectodd.wunderboss.ApplicationRunner;

public class TorqueBoxMain {

    public static void main(String[] args) throws Throwable {
        TorqueBoxMain torqueBoxMain = new TorqueBoxMain();
        torqueBoxMain.run(args);
    }

    public void run(String[] args) throws Exception {
        new ApplicationRunner("application").start(args);
    }
}
