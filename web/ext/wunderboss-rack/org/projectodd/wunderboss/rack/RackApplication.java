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

import org.jruby.NativeException;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyException;
import org.jruby.RubyHash;
import org.jruby.RubyModule;
import org.jruby.exceptions.RaiseException;
import org.jruby.runtime.Helpers;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.projectodd.wunderboss.ruby.RubyHelper;

import java.io.IOException;
import java.io.InputStream;

public class RackApplication {

    public RackApplication(IRubyObject rubyRackApp) throws IOException {
        this.rubyRackApp = rubyRackApp;
        runtime = rubyRackApp.getRuntime();
        responseModule = RubyHelper.getClass(runtime, RESPONSE_HANDLER_CLASS_NAME);
        rackChannelClass = RackChannel.createRackChannelClass(runtime);
        rackResponderClass = RackResponder.createRackResponderClass(runtime);
        rackHijackClass = RackHijack.createRackHijackClass(runtime);
        rackEnvironment = new RackEnvironment(runtime, rackHijackClass);
    }

    public void call(RackAdapter rackAdapter, RackEnvironment.RACK_KEY sessionKey, Object session) throws Exception {
        RackChannel inputChannel = new RackChannel(runtime, rackChannelClass, rackAdapter.getInputStream());
        boolean hijacked = false;
        try {
            RackEnvironmentHash rackEnvHash = rackEnvironment.getEnv(rackAdapter, inputChannel);
            rackEnvHash.lazyPut(sessionKey, session, false);
            ThreadContext threadContext = runtime.getCurrentContext();
            IRubyObject rackResponse = rubyRackApp.callMethod(threadContext, "call", rackEnvHash);
            RackResponder rackResponder = new RackResponder(runtime, rackResponderClass, rackAdapter);
            Helpers.invoke(threadContext, responseModule, RESPONSE_HANDLER_METHOD_NAME,
                    rackResponse, rackResponder, rackEnvHash);
        } catch (RackHijackException ex) {
            hijacked = true;
        } finally {
            // Closing the RackChannel cleans up the temp file resources
            inputChannel.close();
            if (!hijacked) {
                // But only close the InputStream itself if we're not hijacked
                rackAdapter.getInputStream().close();
            }
        }
    }

    private IRubyObject rubyRackApp;
    private Ruby runtime;
    private RubyModule responseModule;
    private RubyClass rackChannelClass;
    private RubyClass rackResponderClass;
    private RubyClass rackHijackClass;
    private RackEnvironment rackEnvironment;

    public static final String RESPONSE_HANDLER_CLASS_NAME = "WunderBoss::Rack::ResponseHandler";
    public static final String RESPONSE_HANDLER_METHOD_NAME = "handle";
}
