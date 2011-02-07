package org.torquebox.interp.core;

import org.jruby.Ruby;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.test.mc.vfs.AbstractVFSTestCase;

public class VFSLoadServiceWithRuntimeTest extends AbstractVFSTestCase {

    private VFSLoadService loadService;
    private Ruby ruby;

    @Before
    public void setUp() throws Exception {
        RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl();
        factory.setUseJRubyHomeEnvVar( false );
        this.ruby = factory.create();
        this.loadService = (VFSLoadService) this.ruby.getLoadService();
    }

    @Test
    public void testRubygemsLoadable() throws Exception {
        this.loadService.require( "rubygems" );
    }

}
