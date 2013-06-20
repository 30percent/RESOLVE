/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.clemson.cs.r2jt.translation.CTranslationBookkeeping;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mark T
 */
public class CFunctionBookkeeper {
    String returnType;
    String functionName;
    String returnName;
    List<String> params;

    List<String> varInit;
    StringBuffer allStmts;
    
    // Boolean location checks
    boolean inParamList = false;
    
    public CFunctionBookkeeper(String newName, String newReturnTy){

        returnType = "void ";
        returnName = newName;
        params = new ArrayList<String>();
        varInit = new ArrayList<String>();
        allStmts = new StringBuffer();
        //ewFunc.functionName = stringFromSym(newFuncName, newFunc.returnType);
    }

    private String getFunctionString() {
        StringBuilder retBuf = new StringBuilder();

        retBuf.append(functionName).append("(");
        int size = params.size() - 1;
        for (int i = 0; i <= size; i++) {
            retBuf.append(params.get(i));
            if (!(i == size)) {
                retBuf.append(", ");
            }
        }
        retBuf.append("){");
        if (!returnType.equals("void ")) {
            //retBuf.append(returnType).append(returnName).append(returnType[1]).append(";");
        }
        for (String a : varInit) {
            retBuf.append(a).append(";");
        }
        if (allStmts != null) {
            retBuf.append(allStmts);
        }
        if (!returnType.equals("void ")) {
            retBuf.append("return ").append(returnName).append(";");
        }
        retBuf.append(" }");

        return retBuf.toString();

    }
}
