/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.clemson.cs.r2jt.translation;

import edu.clemson.cs.r2jt.absyn.FacilityOperationDec;
import edu.clemson.cs.r2jt.absyn.ModuleDec;
import edu.clemson.cs.r2jt.absyn.NameTy;
import edu.clemson.cs.r2jt.errors.ErrorHandler;
import edu.clemson.cs.r2jt.init.CompileEnvironment;
import edu.clemson.cs.r2jt.scope.OldSymbolTable;
import edu.clemson.cs.r2jt.treewalk.TreeWalkerStackVisitor;
import edu.clemson.cs.r2jt.utilities.Flag;

/**
 *
 * @author Mark T
 */
public class CTranslator extends TreeWalkerStackVisitor {

    /*
     * Variable Declaration
     */

    CTranslatorInfo cInfo;
    private ErrorHandler err;
    private String targetFileName;
    private boolean isMath;

    //Flags
    private static final String FLAG_SECTION_NAME = "Pretty C Translation";

    private static final String FLAG_DESC_TRANSLATE =
            "Translates into " + "a \"Pretty\" C version following the line numbers of the "
                    + "RESOLVE Facility.";
    public static final Flag FLAG_PRETTY_C_TRANSLATE =
            new Flag(FLAG_SECTION_NAME, "prettyCTranslate", FLAG_DESC_TRANSLATE);

    //Global stmt buf
    StringBuffer stmtBuf;

    /*
     * End of Variable Declaration
     */

    public CTranslator(ModuleDec dec, ErrorHandler err) {
        this.err = err;
        targetFileName = dec.getName().getFile().getName();
        cInfo = new CTranslatorInfo(dec.getName().getFile().getName());
        stmtBuf = new StringBuffer();
        isMath = false;
    }

    /* Visitor Methods */

    @Override
    /**
     * Facility Operations.
     * For Facility Operations, there are no representation types.
     */
    public void preFacilityOperationDec(FacilityOperationDec dec) {
        NameTy retTy = null;
        if (dec.getReturnTy() != null) {
            retTy = (NameTy) dec.getReturnTy();
            cInfo.addFunction(dec.getName(), retTy.getName());
        }
        else {
            cInfo.addFunction(dec.getName(), null);
        }
    }

}
