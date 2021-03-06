package edu.clemson.cs.r2jt.typeandpopulate;

import edu.clemson.cs.r2jt.typeandpopulate.query.UniversalVariableQuery;
import edu.clemson.cs.r2jt.typeandpopulate.entry.MathSymbolEntry;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class SymmetricBoundVariableVisitor extends SymmetricVisitor {

    private static final NoSuchElementException NO_SUCH_ELEMENT = new NoSuchElementException();

    private Deque<Map<String, MTType>> myBoundVariables1 = new LinkedList<Map<String, MTType>>();

    private Deque<Map<String, MTType>> myBoundVariables2 = new LinkedList<Map<String, MTType>>();

    public SymmetricBoundVariableVisitor() {

    }

    public SymmetricBoundVariableVisitor(FinalizedScope context1) {
        Map<String, MTType> topLevel = new HashMap<String, MTType>();

        List<MathSymbolEntry> quantifiedVariables = context1.query(UniversalVariableQuery.INSTANCE);
        for (MathSymbolEntry entry : quantifiedVariables) {
            topLevel.put(entry.getName(), entry.getType());
        }

        myBoundVariables1.push(topLevel);
    }

    public SymmetricBoundVariableVisitor(Map<String, MTType> context1) {
        myBoundVariables1.push(new HashMap<String, MTType>(context1));
    }

    public SymmetricBoundVariableVisitor(Map<String, MTType> context1, Map<String, MTType> context2) {
        this(context1);
        myBoundVariables2.push(new HashMap<String, MTType>(context2));
    }

    public void reset() {
        myBoundVariables1.clear();
        myBoundVariables2.clear();
    }

    public final boolean beginMTBigUnion(MTBigUnion t1, MTBigUnion t2) {
        myBoundVariables1.push(t1.getQuantifiedVariables());
        myBoundVariables2.push(t2.getQuantifiedVariables());
        return boundBeginMTBigUnion(t1, t2);
    }

    public final boolean endMTBigUnion(MTBigUnion t1, MTBigUnion t2) {
        boolean result = boundEndMTBigUnion(t1, t2);
        myBoundVariables1.pop();
        myBoundVariables2.pop();

        return result;
    }

    public boolean boundBeginMTBigUnion(MTBigUnion t1, MTBigUnion t2) {
        return true;
    }

    public boolean boundEndMTBigUnion(MTBigUnion t1, MTBigUnion t2) {
        return true;
    }

    public MTType getInnermostBinding1(String name) {
        return getInnermostBinding(myBoundVariables1, name);
    }

    public MTType getInnermostBinding2(String name) {
        return getInnermostBinding(myBoundVariables2, name);
    }

    private static MTType getInnermostBinding(Deque<Map<String, MTType>> scopes, String name) {

        MTType result = null;

        Iterator<Map<String, MTType>> scopesIter = scopes.iterator();
        while (result == null && scopesIter.hasNext()) {
            result = scopesIter.next().get(name);
        }

        if (result == null) {
            throw new NoSuchElementException(name);
        }

        return result;
    }
}
