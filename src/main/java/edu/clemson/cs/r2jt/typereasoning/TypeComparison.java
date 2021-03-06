package edu.clemson.cs.r2jt.typereasoning;

import edu.clemson.cs.r2jt.absyn.Exp;
import edu.clemson.cs.r2jt.typeandpopulate.MTType;

public interface TypeComparison<V extends Exp, T extends MTType> {

    public boolean compare(V foundValue, T foundType, T expectedType);

    public String description();
}
