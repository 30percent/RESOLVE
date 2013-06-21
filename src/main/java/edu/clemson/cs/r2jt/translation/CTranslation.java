/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.clemson.cs.r2jt.translation;

import edu.clemson.cs.r2jt.absyn.FacilityOperationDec;
import edu.clemson.cs.r2jt.absyn.ModuleDec;
import edu.clemson.cs.r2jt.absyn.NameTy;
import edu.clemson.cs.r2jt.absyn.ParameterVarDec;
import edu.clemson.cs.r2jt.absyn.VarDec;
import edu.clemson.cs.r2jt.data.PosSymbol;
import edu.clemson.cs.r2jt.errors.ErrorHandler;
import edu.clemson.cs.r2jt.translation.CTranslationBookkeeping.CTranslationBookkeeper;
import edu.clemson.cs.r2jt.treewalk.TreeWalkerStackVisitor;
import edu.clemson.cs.r2jt.typeandpopulate.MathSymbolTable;
import edu.clemson.cs.r2jt.typeandpopulate.ModuleScope;
import edu.clemson.cs.r2jt.typeandpopulate.ScopeRepository;
import edu.clemson.cs.r2jt.utilities.Flag;

/**
 *
 * @author Mark T
 */
public class CTranslation extends TreeWalkerStackVisitor {

    /*
     * Variable Declaration
     */

    CTranslationBookkeeper cInfo;
    private ErrorHandler err;
    private String targetFileName;
    private boolean isMath;
    ModuleScope myScope;
    MathSymbolTable symbolTable;

    //Flags
    private static final String FLAG_SECTION_NAME = "Full C Translation";

    private static final String FLAG_DESC_TRANSLATE =
            "Translates into " + "a \"Full\" C version of the " + "RESOLVE File.";
    public static final Flag FLAG_C_TRANSLATE =
            new Flag(FLAG_SECTION_NAME, "ctranslate", FLAG_DESC_TRANSLATE);

    /*
     * End of Variable Declaration
     */

    public CTranslation(ModuleScope newScope, ModuleDec dec, ErrorHandler err) {
        myScope = newScope;
        this.err = err;
        targetFileName = dec.getName().getFile().getName();
        cInfo = new CTranslationBookkeeper(dec.getName().getFile().getName());
        isMath = false;
        QueryFake();
    }

    private void QueryFake() {

    }

    /* Visitor Methods */

    @Override
    /**
     * Facility Operations.
     * For Facility Operations, there are no representation types.
     */
    public void preFacilityOperationDec(FacilityOperationDec dec) {
        NameTy retTy;
        if (dec.getReturnTy() != null) {
            retTy = (NameTy) dec.getReturnTy();
            //TODO: Handle return types;
            cInfo.addFunction(dec.getName().getName(), retTy.getName().getName());
        }
        else {
            cInfo.addFunction(dec.getName().getName(), "void ");
        }
    }

    @Override
    public void postFacilityOperationDec(FacilityOperationDec dec) {
        cInfo.endFunction();
    }

    @Override
    public void preVarDec(VarDec dec) {
        PosSymbol name = dec.getName();
        if (dec.getTy() instanceof NameTy) {
            NameTy ty = (NameTy) dec.getTy();
            String stTy = ty.getName().getName();
            String newTy, init;
        }

    }

    @Override
    public void preParameterVarDec(ParameterVarDec dec) {
        StringBuilder parmSet = new StringBuilder();
        if (dec.getTy() instanceof NameTy) {
            //parmSet.append(cInfo.getCVarsWithLines(((NameTy) dec.getTy()).getName(), null));
        }
        else {
            System.out.println("How did you reach here?");
        }
        // parmSet.append(" ").append(cInfo.stringFromSym(dec.getName(), null));
        //cInfo.addParamToFunc(parmSet.toString());
        //myScope.queryForOne(new NameQuery());
    }

}
