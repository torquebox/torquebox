package org.torquebox.interp.analysis;

import java.io.IOException;
import java.io.InputStream;

import org.jruby.Ruby;
import org.jruby.ast.Node;
import org.jruby.ast.visitor.NodeVisitor;
import org.jruby.parser.LocalStaticScope;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.DynamicScope;
import org.jruby.runtime.scope.ManyVarsDynamicScope;

public class ScriptAnalyzer {

    public ScriptAnalyzer() {
    }
    
    public Object analyze(String filename, InputStream in, NodeVisitor visitor) throws IOException {
        StringBuffer script = new StringBuffer();
        
        int numRead = 0;
        byte[] buf = new byte[1024];
        
        while ( ( numRead = in.read( buf ))  >= 0 ) {
            script.append( new String( buf, 0, numRead ) );
        }
        
        return analyze( filename, script.toString(), visitor );
    }
    
    public Object analyze(String filename, String script, NodeVisitor visitor) {
        Ruby ruby = Ruby.newInstance();
        StaticScope staticScope = new LocalStaticScope( null );
        DynamicScope scope = new ManyVarsDynamicScope( staticScope );
        Node result = ruby.parseEval( script, filename, scope, 0 );
        return result.accept( visitor );
    }
}