package edu.clemson.cs.r2jt.typeandpopulate;

import edu.clemson.cs.r2jt.absyn.ResolveConceptualElement;

@SuppressWarnings("serial")
public class NoSuchScopeException extends RuntimeException {

    public final ResolveConceptualElement requestedScope;

    public NoSuchScopeException(ResolveConceptualElement e) {
        requestedScope = e;
    }
}
