package edu.clemson.cs.r2jt.typeandpopulate;

import edu.clemson.cs.r2jt.typereasoning.TypeGraph;

public abstract class MTAbstract<T extends MTType> extends MTType {

    public MTAbstract(TypeGraph typeGraph) {
        super(typeGraph);
    }
}
