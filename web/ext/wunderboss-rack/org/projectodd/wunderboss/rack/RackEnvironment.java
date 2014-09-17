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

package org.projectodd.wunderboss.rack;

import org.jboss.logging.Logger;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyBoolean;
import org.jruby.RubyFixnum;
import org.jruby.RubyHash;
import org.jruby.RubyIO;
import org.jruby.RubyString;
import org.projectodd.wunderboss.ruby.RubyHelper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RackEnvironment {

    // When adding a key to the enum be sure to add its RubyString equivalent
    // to populateRackKeyMap below
    static enum RACK_KEY {
        RACK_INPUT, RACK_ERRORS, REQUEST_METHOD, SCRIPT_NAME,
        PATH_INFO, QUERY_STRING, SERVER_NAME, SERVER_PORT,
        CONTENT_TYPE, REQUEST_URI, REMOTE_ADDR, URL_SCHEME,
        VERSION, MULTITHREAD, MULTIPROCESS, RUN_ONCE, CONTENT_LENGTH,
        HTTPS
    }
    static final int NUM_RACK_KEYS = RACK_KEY.values().length;

    public RackEnvironment(final Ruby runtime) throws IOException {
        this.runtime = runtime;
        rackVersion = RubyArray.newArray(runtime);
        rackVersion.add(RubyFixnum.one(runtime));
        rackVersion.add(RubyFixnum.one(runtime));
        errors = new RubyIO(runtime, runtime.getErr());
        errors.setAutoclose(false);

        populateRackKeyMap();
    }

    private void populateRackKeyMap() {
        putRack("rack.input", RACK_KEY.RACK_INPUT);
        putRack("rack.errors", RACK_KEY.RACK_ERRORS);
        putRack("REQUEST_METHOD", RACK_KEY.REQUEST_METHOD);
        putRack("SCRIPT_NAME", RACK_KEY.SCRIPT_NAME);
        putRack("PATH_INFO", RACK_KEY.PATH_INFO);
        putRack("QUERY_STRING", RACK_KEY.QUERY_STRING);
        putRack("SERVER_NAME", RACK_KEY.SERVER_NAME);
        putRack("SERVER_PORT", RACK_KEY.SERVER_PORT);
        putRack("CONTENT_TYPE", RACK_KEY.CONTENT_TYPE);
        putRack("REQUEST_URI", RACK_KEY.REQUEST_URI);
        putRack("REMOTE_ADDR", RACK_KEY.REMOTE_ADDR);
        putRack("rack.url_scheme", RACK_KEY.URL_SCHEME);
        putRack("rack.version", RACK_KEY.VERSION);
        putRack("rack.multithread", RACK_KEY.MULTITHREAD);
        putRack("rack.multiprocess", RACK_KEY.MULTIPROCESS);
        putRack("rack.run_once", RACK_KEY.RUN_ONCE);
        putRack("CONTENT_LENGTH", RACK_KEY.CONTENT_LENGTH);
        putRack("HTTPS", RACK_KEY.HTTPS);
    }

    private void putRack(String key, RACK_KEY value) {
        rackKeyMap.put(RubyHelper.toUsAsciiRubyString(runtime, key), value);
    }

    public RubyHash getEnv(final RackAdapter rackAdapter,
                           final RackChannel inputChannel) throws IOException {
        // TODO: Should we only use this faster RackEnvironmentHash if we detect
        // specific JRuby versions that we know are compatible?
        final RackEnvironmentHash env = new RackEnvironmentHash(runtime, rackAdapter, rackKeyMap);
        env.lazyPut(RACK_KEY.RACK_INPUT, inputChannel, false);
        env.lazyPut(RACK_KEY.RACK_ERRORS, errors, false);

        // Don't use request.getPathInfo because that gets decoded by the container
        String pathInfo = rackAdapter.getPathInfo();

        String scriptName = rackAdapter.getScriptName();
        // SCRIPT_NAME should be an empty string for the root
        if (scriptName.equals("/")) {
            scriptName = "";
        }

        // For performance reasons, a lot of these Rack keys have been moved
        // into RackEnvironmentHash where their values are lazily computed
        // We should probably move everything over just to get all this code
        // in one place
        env.lazyPut(RACK_KEY.REQUEST_METHOD, rackAdapter.getRequestMethod(), true);
        env.lazyPut(RACK_KEY.SCRIPT_NAME, scriptName, false);
        env.lazyPut(RACK_KEY.PATH_INFO, pathInfo, false);
        env.lazyPut(RACK_KEY.QUERY_STRING, rackAdapter.getQueryString(), false);
        env.lazyPut(RACK_KEY.REQUEST_URI, scriptName + pathInfo, false);
        env.lazyPut(RACK_KEY.URL_SCHEME, rackAdapter.getScheme(), true);
        env.lazyPut(RACK_KEY.VERSION, rackVersion, false);
        env.lazyPut(RACK_KEY.MULTITHREAD, RubyBoolean.newBoolean(runtime, true), false);
        env.lazyPut(RACK_KEY.MULTIPROCESS, RubyBoolean.newBoolean(runtime, true), false);
        env.lazyPut(RACK_KEY.RUN_ONCE, RubyBoolean.newBoolean(runtime, false), false);

        if ("https".equals(rackAdapter.getScheme())) {
            env.lazyPut(RACK_KEY.HTTPS, "on", true);
        }

        return env;
    }

    private final Ruby runtime;
    private final RubyArray rackVersion;
    private final RubyIO errors;
    private final Map<RubyString, RACK_KEY> rackKeyMap = new HashMap<>();

    private static final Logger log = Logger.getLogger(RackEnvironment.class);

}
