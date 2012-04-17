/*
 * Copyright 2008-2012 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.torquebox.core.analysis;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.jboss.logging.Logger;
import org.jboss.vfs.VirtualFile;
import org.jruby.CompatVersion;
import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig;
import org.jruby.RubyProc;
import org.jruby.ast.Node;
import org.jruby.ast.visitor.NodeVisitor;
import org.jruby.exceptions.RaiseException;
import org.jruby.parser.LocalStaticScope;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.BlockBody;
import org.jruby.runtime.DynamicScope;
import org.jruby.runtime.Interpreted19Block;
import org.jruby.runtime.InterpretedBlock;
import org.jruby.runtime.scope.ManyVarsDynamicScope;
import org.torquebox.core.runtime.RubyRuntimeMetaData.Version;

/**
 * Ruby script analyzer capable of applying an <code>AnalyzingVisitor</code> to
 * ruby scripts, in either 1_8 or 1_9 modes.
 * 
 * @see NodeVisitor
 * 
 * @author Toby Crawley
 * @author Bob McWhirter
 */
public class ScriptAnalyzer {

    /** Cached ruby 1.8 interpreter. */
    private Ruby ruby18;

    /** Cached ruby 1.9 interpreter. */
    private Ruby ruby19;
    
    private Logger log = Logger.getLogger( this.getClass() );

    public ScriptAnalyzer() {
        createRuby18();
        createRuby19();
    }

    protected void createRuby18() {
        RubyInstanceConfig config = new RubyInstanceConfig();
        config.setCompatVersion( CompatVersion.RUBY1_8 );
        config.setLoader( this.getClass().getClassLoader() );
        this.ruby18 = Ruby.newInstance( config );
    }

    protected void createRuby19() {
        RubyInstanceConfig config = new RubyInstanceConfig();
        config.setCompatVersion( CompatVersion.RUBY1_9 );
        config.setLoader( this.getClass().getClassLoader() );
        this.ruby19 = Ruby.newInstance( config );
    }

    public void destroy() {
        destroyRuby19();
        destroyRuby18();
    }

    protected void destroyRuby18() {
        if (this.ruby18 != null) {
            this.ruby18.tearDown( false );
        }
    }

    protected void destroyRuby19() {
        if (this.ruby19 != null) {
            this.ruby19.tearDown( false );
        }
    }

    public void analyze(VirtualFile file, NodeVisitor visitor, Version rubyVersion) throws IOException {
        if (!file.exists()) {
            return;
        }
        InputStream in = null;

        try {
            in = file.openStream();
            analyze( file.getPathName(), in, visitor, rubyVersion );
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public void analyze(String filename, InputStream in, NodeVisitor visitor, Version rubyVersion) throws IOException {
        StringBuffer script = new StringBuffer();

        int numRead = 0;
        byte[] buf = new byte[1024];

        while ((numRead = in.read( buf )) >= 0) {
            script.append( new String( buf, 0, numRead ) );
        }

        analyze( filename, script.toString(), visitor, rubyVersion );
    }

    /**
     * Analyze a script, given a visitor and Ruby interpreter version.
     * 
     * @param filename The name of the file for <code>__FILE__</code>.
     * @param script The contents of the script to analyze.
     * @param visitor The visitor to apply to the Ruby AST.
     * @param rubyVersion The version of Ruby interpreter to use during
     *            analysis.
     * @return The result provided by the specific <code>NodeVisitor</code>.
     */
    public void analyze(String filename, String script, NodeVisitor visitor, Version rubyVersion) {
        try {
            // Temporary reflection hack until LocalStaticScope's constructor is visible again
            Constructor<LocalStaticScope> constructor = LocalStaticScope.class.getDeclaredConstructor( StaticScope.class );
            constructor.setAccessible( true );
            StaticScope staticScope = constructor.newInstance( new Object[] { null } );
            DynamicScope scope = new ManyVarsDynamicScope( staticScope );
            Ruby analyzingRuby = null;

            if (rubyVersion.equals( Version.V1_8 )) {
                analyzingRuby = this.ruby18;
            } else if (rubyVersion.equals( Version.V1_9 )) {
                analyzingRuby = this.ruby19;
            }

            try {
                Node result = analyzingRuby.parseEval( script, filename, scope, 0 );
                result.accept( visitor );
            } catch(RaiseException ex) {
                log.trace( "JRuby exception when parsing file " + filename , ex );
            }
        }
        // Catch the reflection-related exceptions until LocalStaticScope's
        // constructor is public again
        catch (NoSuchMethodException e) {}
        catch (InvocationTargetException e) {}
        catch (IllegalAccessException e) {}
        catch (InstantiationException e) {}
    }

    /**
     * Analyze a script, given a visitor and Ruby interpreter version.
     * 
     * @param filename The name of the file for <code>__FILE__</code>.
     * @param script The contents of the script to analyze.
     * @param visitor The visitor to apply to the Ruby AST.
     * @param rubyVersion The version of Ruby interpreter to use during
     *            analysis.
     * @return The result provided by the specific <code>NodeVisitor</code>.
     */
    public void analyze(RubyProc proc, NodeVisitor visitor) {
        BlockBody body = proc.getBlock().getBody();
        if (body instanceof InterpretedBlock ) {
            Node result = ((InterpretedBlock)body).getBodyNode();
            result.accept( visitor );
        } else if ( body instanceof Interpreted19Block ) {
            Node result = ((Interpreted19Block)body).getBody();
            result.accept( visitor );
        } else {
            System.err.println( "Unable to analyze: " + body.getClass() );
        }

    }
}
