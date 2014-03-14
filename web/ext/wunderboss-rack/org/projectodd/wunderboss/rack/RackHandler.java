package org.projectodd.wunderboss.rack;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyHash;
import org.jruby.RubyModule;
import org.jruby.runtime.Helpers;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.projectodd.wunderboss.ruby.RubyHelper;

import java.io.IOException;

public class RackHandler implements HttpHandler {

    public RackHandler(final IRubyObject rackApplication, final String context) throws IOException {
        this.rackApplication = rackApplication;
        this.context = context;
        runtime = rackApplication.getRuntime();
        responseModule = RubyHelper.getClass(runtime, RESPONSE_HANDLER_CLASS_NAME);
        rackChannelClass = RackChannel.createRackChannelClass(runtime);
        rackResponderClass = RackResponder.createRackResponderClass(runtime);
        rackEnvironment = new RackEnvironment(runtime);
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        exchange.startBlocking();
        RackChannel inputChannel = null;
        try {
            inputChannel = new RackChannel(runtime, rackChannelClass, exchange.getInputStream());
            RubyHash rackEnvHash = rackEnvironment.getEnv(exchange, inputChannel, context);
            ThreadContext threadContext = runtime.getCurrentContext();
            IRubyObject rackResponse = rackApplication.callMethod(threadContext, "call", rackEnvHash);
            RackResponder rackResponder = new RackResponder(runtime, rackResponderClass, exchange);
            Helpers.invoke(threadContext, responseModule, RESPONSE_HANDLER_METHOD_NAME, rackResponse, rackResponder);
        } finally {
            if (inputChannel != null) {
                inputChannel.close();
            }
            exchange.endExchange();
        }
    }

    private IRubyObject rackApplication;
    private String context;
    private Ruby runtime;
    private RubyModule responseModule;
    private RubyClass rackChannelClass;
    private RubyClass rackResponderClass;
    private RackEnvironment rackEnvironment;

    public static final String RESPONSE_HANDLER_CLASS_NAME = "WunderBoss::Rack::ResponseHandler";
    public static final String RESPONSE_HANDLER_METHOD_NAME = "handle";
}
