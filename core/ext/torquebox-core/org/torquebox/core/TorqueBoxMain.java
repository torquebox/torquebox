/*
* Copyright 2014 Red Hat, Inc, and individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.torquebox.core;

import org.projectodd.wunderboss.ApplicationRunner;

public class TorqueBoxMain {

    public static void main(String[] args) throws Throwable {
        TorqueBoxMain torqueBoxMain = new TorqueBoxMain();
        try {
            torqueBoxMain.run(args);
        } catch (Exception ex) {
            checkForSystemExit(ex);
            throw ex;
        }
    }

    public void run(String[] args) throws Exception {
        System.setProperty("torquebox.app_jar", jarPath());
        new ApplicationRunner("application").start(args);
    }

    private String jarPath() {
        String mainPath = ApplicationRunner.class.getName().replace(".", "/") + ".class";
        String mainUrl = ApplicationRunner.class.getClassLoader().getResource(mainPath).toString();
        int from = "jar:file:".length();
        int to = mainUrl.indexOf("!/");
        return mainUrl.substring(from, to);
    }

    /*
     * Catch any RubySystemExit exceptions and propagate the exit
     * status to System.exit using reflection because JRuby libs
     * are not visible at this point.
     * Any changes to this need to be copied to the same method in
     * TorqueBoxWarMain.java
     */
    public static void checkForSystemExit(Exception ex) throws Exception {
        if (ex.getClass().getName().equals("org.jruby.exceptions.RaiseException")) {
            Object rubyException = ex.getClass().getDeclaredMethod("getException").invoke(ex);
            if (rubyException.getClass().getName().equals("org.jruby.RubySystemExit")) {
                Object rubyStatus = rubyException.getClass().
                        getDeclaredMethod("status").invoke(rubyException);
                int status = 0;
                if (rubyStatus != null) {
                    Long longStatus = (Long) rubyStatus.getClass().
                            getDeclaredMethod("getLongValue").invoke(rubyStatus);
                    status = longStatus.intValue();
                }
                System.exit(status);
            }
        }
    }
}
