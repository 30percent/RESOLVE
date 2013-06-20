/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.clemson.cs.r2jt.translation.CTranslationBookkeeping;

import edu.clemson.cs.r2jt.data.PosSymbol;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mark T
 */
public class CTranslationBookkeeper {

    private String name;
    private StringBuffer myImportList;
    private List<CFunctionBookkeeper> funcList;
    private CFunctionBookkeeper currentFunc;

    public CTranslationBookkeeper(String newName) {
        name = newName;
        funcList = new ArrayList<CFunctionBookkeeper>();
    }

    public void addFunction(String newFuncName, String newReturnTy) {
        CFunctionBookkeeper newFunc = new CFunctionBookkeeper(newFuncName, newReturnTy);
        funcList.add(newFunc);
        currentFunc = newFunc;
    }
    
    public void appendToStmt(String append) {
        currentFunc.allStmts.append(append);
    }
    
    public void endFunction(){
        currentFunc = null;
    }
}
