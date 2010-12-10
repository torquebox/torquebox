package org.torquebox.integration;

import org.jruby.Ruby;
import org.torquebox.interp.core.RubyRuntimeFactoryImpl;

public class IntegrationTestRubyFactory {

    public static Ruby createRuby() throws Exception {
        RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl();

        factory.setUseJRubyHomeEnvVar(false);
        
        if (System.getProperty("jruby.home") != null) {
            factory.setJRubyHome( System.getProperty( "jruby.home" ) );
        }

        Ruby ruby = factory.create();

        ruby.evalScriptlet("require %q(rubygems)");
        return ruby;
    }

}
