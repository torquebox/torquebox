package org.torquebox.core.injection.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.vfs.VirtualFile;

/**
 * Index of injections requested sets of files.
 * 
 * <p>
 * Each file is indexed independently to enable quick look-up of relevant
 * injections at a later point in time.
 * </p>
 * 
 * @see Injectable
 * 
 * @author Bob McWhirter
 */
public class InjectionIndex {
    public static final AttachmentKey<InjectionIndex> ATTACHMENT_KEY = AttachmentKey.create( InjectionIndex.class );

    /**
     * Construct an index for a given root.
     * 
     * @param root
     *            The root.
     */
    public InjectionIndex(VirtualFile root) {
        this.root = root;
    }

    /**
     * Add injectables for a given file.
     * 
     * <p>
     * This method is purely additive. Calling it several times with different
     * arguments will cause all specified injectables across all calls to be
     * considered applicable for the file.
     * </p>
     * 
     * <p>
     * Adding the same injectable to the same path multiple times has no effect,
     * but causes no error.
     * </p>
     * 
     * @param path
     *            The path to index.
     * @param injectables
     *            The injectables relevant to the path.
     */
    public void addInjectables(VirtualFile path, Set<Injectable> injectables) {
        Set<Injectable> existing = this.index.get( path );

        if (existing == null) {
            existing = new HashSet<Injectable>();
            this.index.put( path.getPathNameRelativeTo( this.root ), existing );
        }

        existing.addAll( injectables );
    }

    /**
     * Retrieve the injecatbles relevant to all paths that match any prefix in
     * the passed-in collection.
     * 
     * <p>
     * Simple starts-with matching is performed against all files in the index
     * against each path prefix in the passed-in collection.
     * </p>
     * 
     * <p>
     * If the path prefix of <code>.</code> (a single period) is included in the
     * collection, all files (non-recursively) located at the root of the index
     * will be considered.
     * </p>
     * 
     * @param pathPrefixes
     *            The prefixes to accumulate.
     * @return All injectables relevant to all files matching any of the
     *         prefixes.
     */
    public Set<Injectable> getInjectablesFor(List<String> pathPrefixes) {
        Set<Injectable> injectables = new HashSet<Injectable>();

        for (Entry<String, Set<Injectable>> entry : this.index.entrySet()) {
            for (String prefix : pathPrefixes) {
                String key = entry.getKey();
                // root ('.') is a special case - only files in the root are
                // matched
                if (key.startsWith( prefix ) ||
                        (".".equals( prefix ) && !key.contains( "/" ))) {
                    injectables.addAll( entry.getValue() );
                }
            }
        }
        return injectables;
    }

    public String toString() {
        return this.index.toString();
    }

    /** Root of the index. */
    private final VirtualFile root;

    /** The underlying index. */
    private final Map<String, Set<Injectable>> index = new HashMap<String, Set<Injectable>>();

}
