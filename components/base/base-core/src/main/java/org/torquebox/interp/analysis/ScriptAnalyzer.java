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

    private NodeVisitor visitor;

    public ScriptAnalyzer() {
    }
    
    public void setNodeVisitor(NodeVisitor visitor) {
        this.visitor = visitor;
    }
    
    public NodeVisitor getNodeVisitor() {
        return this.visitor;
    }
    
    public Object analyze(InputStream in) throws IOException {
        StringBuffer script = new StringBuffer();
        
        int numRead = 0;
        byte[] buf = new byte[1024];
        
        while ( ( numRead = in.read( buf ))  >= 0 ) {
            script.append( new String( buf, 0, numRead ) );
        }
        
        return analyze( script.toString() );
    }
    
    public Object analyze(String script) {
        Ruby ruby = Ruby.newInstance();
        StaticScope staticScope = new LocalStaticScope( null );
        DynamicScope scope = new ManyVarsDynamicScope( staticScope );
        Node result = ruby.parseEval( script, "<analyzing>", scope, 0 );
        return result.accept( this.visitor );
    }
}