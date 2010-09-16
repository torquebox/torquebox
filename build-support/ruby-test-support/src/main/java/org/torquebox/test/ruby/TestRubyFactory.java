package org.torquebox.test.ruby;

import org.jruby.Ruby;
import org.torquebox.interp.core.RubyRuntimeFactoryImpl;

public class TestRubyFactory {

    public static Ruby createRuby() throws Exception {
        RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl();

        if (System.getProperty("gem.path") != null) {
            factory.setGemPath( System.getProperty( "gem.path" ) );
        } else {
            factory.setGemPath("target/rubygems");
        }
        factory.setUseJRubyHomeEnvVar(false);

        Ruby ruby = factory.create();

        ruby.evalScriptlet("require %q(rubygems)");
        return ruby;
    }

}
