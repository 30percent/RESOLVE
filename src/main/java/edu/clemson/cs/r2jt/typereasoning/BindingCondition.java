package edu.clemson.cs.r2jt.typereasoning;

import edu.clemson.cs.r2jt.absyn.*;

public class BindingCondition {

    private Exp myConditionExp;

    public BindingCondition(Exp condition) {
        myConditionExp = condition;
    }

    @Override
    public String toString() {
        return myConditionExp.toString();
    }
}
