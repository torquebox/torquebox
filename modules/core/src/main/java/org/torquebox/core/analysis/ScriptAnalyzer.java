package org.torquebox.core.analysis;

import java.io.IOException;
import java.io.InputStream;

import org.jboss.vfs.VirtualFile;
import org.jruby.CompatVersion;
import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig;
import org.jruby.ast.Node;
import org.jruby.ast.visitor.NodeVisitor;
import org.jruby.parser.LocalStaticScope;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.DynamicScope;
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

    /** Analyze a script, given a visitor and Ruby interpreter version.
     * 
     * @param filename The name of the file for <code>__FILE__</code>.
     * @param script The contents of the script to analyze.
     * @param visitor The visitor to apply to the Ruby AST.
     * @param rubyVersion The version of Ruby interpreter to use during analysis.
     * @return The result provided by the specific <code>NodeVisitor</code>.
     */
    public void analyze(String filename, String script, NodeVisitor visitor, Version rubyVersion) {
        StaticScope staticScope = new LocalStaticScope( null );
        DynamicScope scope = new ManyVarsDynamicScope( staticScope );
        Ruby analyzingRuby = null;

        if (rubyVersion.equals( Version.V1_8 )) {
            analyzingRuby = this.ruby18;
        } else if (rubyVersion.equals( Version.V1_9 )) {
            analyzingRuby = this.ruby19;
        }

        Node result = analyzingRuby.parseEval( script, filename, scope, 0 );
        result.accept( visitor );
    }
}
