package edu.clemson.cs.r2jt.typeandpopulate;

import java.util.List;

import edu.clemson.cs.r2jt.typereasoning.TypeGraph;

public interface FunctionApplicationFactory {

    public MTType buildFunctionApplication(TypeGraph g, MTFunction f, String calledAsName,
            List<MTType> arguments);
}
