/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.clemson.cs.r2jt.translation;

import edu.clemson.cs.r2jt.data.PosSymbol;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mark T
 */
public class CTranslatorInfo {

    private String name;
    private StringBuffer myImportList;
    private List<Function> funcList;
    private Function currentFunc;

    public CTranslatorInfo(String newName) {
        name = newName;
        funcList = new ArrayList<Function>();
    }

    class Function {

        String returnType;
        String functionName;
        String returnName;
        List<String> params;

        List<String> varInit;
        List<String> stmts;
        StringBuffer allStmt;

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
            if (allStmt != null) {
                retBuf.append(allStmt);
            }
            if (!returnType.equals("void ")) {
                retBuf.append("return ").append(returnName).append(";");
            }
            retBuf.append(" }");

            return retBuf.toString();

        }
    }

    public void addFunction(PosSymbol newFuncName, PosSymbol newReturnTy) {
        CTranslatorInfo.Function newFunc = new CTranslatorInfo.Function();

        //newFunc.returnType = new String[2];
        newFunc.returnType = "void ";
        if (newReturnTy != null) {
            //WARN
            //newFunc.returnType = getCVarType(newReturnTy.getName());
            newFunc.returnType = "RETURNTYPE";
        }
        newFunc.returnName = newFuncName.getName();
        //ewFunc.functionName = stringFromSym(newFuncName, newFunc.returnType);
        funcList.add(newFunc);
        newFunc.params = new ArrayList<String>();
        newFunc.stmts = new ArrayList<String>();
        newFunc.varInit = new ArrayList<String>();
        newFunc.allStmt = new StringBuffer();
        currentFunc = newFunc;
    }
}
