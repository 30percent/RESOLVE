package edu.clemson.cs.r2jt.typeandpopulate.searchers;

import edu.clemson.cs.r2jt.typeandpopulate.DuplicateSymbolException;
import edu.clemson.cs.r2jt.typeandpopulate.SymbolTable;
import edu.clemson.cs.r2jt.typeandpopulate.entry.SymbolTableEntry;
import java.util.List;

/**
 * <p>A <code>TableSearcher</code> is a strategy for searching a 
 * {@link SymbolTable SymbolTable}, adding any 
 * {@link SymbolTableEntry SymbolTableEntry}s that match the search to an
 * accumulator.</p>
 *
 * @param <E> Permits concrete implementations of this interface to refine the
 *            type of <code>SymbolTableEntry</code> they will match.  This 
 *            searcher guarantees that any entry it matches will descend from 
 *            <code>E</code>.  Put another way: no matched entry will not be 
 *            a subtype of <code>E</code>.
 */
public interface TableSearcher<E extends SymbolTableEntry> {

    public static enum SearchContext {
        GLOBAL, SOURCE_MODULE, IMPORT, FACILITY
    };

    /**
     * <p>Adds any symbol table entries from <code>entries</code> that match
     * this search to <code>matches</code>.  The order that they are added is
     * determined by the concrete base-class.</p>
     * 
     * <p>If no matches exist, the method will simply leave <code>matches</code>
     * unmodified.</p>
     * 
     * <p>The semantics of the incoming accumulator are only that it is the
     * appropriate place to add new matches, not that it will necessarily 
     * contain all matches so far.  This allows intermediate accumulators to
     * be created and passed without causing strange behavior.  <em>No concrete
     * subclass should depend on the incoming value of the accumulator, save
     * that it will be non-<code>null</code> and mutable.</em></p>
     * 
     * @param entries The set of symbol table entries to consider.
     * @param matches A non-<code>null</code> accumulator of matches.
     * @param l The context from which <code>entries</code> was drawn.
     * 
     * @return <code>true</code> if <code>matches</code> now represents a
     *         final list of search results&mdash;i.e., no further symbol table
     *         entries should be considered.  <code>false</code> indicates that
     *         the search should continue, provided there are additional
     *         un-searched scopes.
     *         
     * @throws DuplicateSymbolException If more than one match is found in
     *         <code>entries</code> where no more than one was expected.
     */
    public boolean addMatches(SymbolTable entries, List<E> matches, SearchContext l)
            throws DuplicateSymbolException;
}
