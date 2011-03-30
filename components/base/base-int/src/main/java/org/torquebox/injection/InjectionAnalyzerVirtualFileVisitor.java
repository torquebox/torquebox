package org.torquebox.injection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileVisitor;
import org.jboss.vfs.VisitorAttributes;

public class InjectionAnalyzerVirtualFileVisitor implements VirtualFileVisitor {
    
    public InjectionAnalyzerVirtualFileVisitor(InjectionAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    @Override
    public VisitorAttributes getAttributes() {
        return VisitorAttributes.LEAVES_ONLY;
    }

    @Override
    public void visit(VirtualFile file) {
        if ( file.getName().endsWith(  ".rb"  ) ) {
            try {
                List<Injectable> injectables = this.analyzer.analyze( file );
                this.injectables.addAll( injectables );
            } catch (IOException e) {
                throw new InjectionException( e );
            }
        }
    }
    
    public List<Injectable> getInjectables() {
        return this.injectables;
    }

    private InjectionAnalyzer analyzer;
    private List<Injectable> injectables = new ArrayList<Injectable>();

}
