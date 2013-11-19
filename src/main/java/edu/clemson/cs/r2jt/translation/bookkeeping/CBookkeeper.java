/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.clemson.cs.r2jt.translation.bookkeeping;

import java.util.Iterator;

/**
 *
 * @author
 * Mark
 * T
 */
public class CBookkeeper extends AbstractBookkeeper {

    /**
     * Construct a supervisor to manage Java modules undergoing 
     * translation.
     */

    protected String conceptName;

    /*C SPECIFIC HELPERS */
    public void setConceptName(String name) {
        conceptName = name;
    }

    public void getConceptName(String name) {
        conceptName = name;
    }

    public CBookkeeper(String name, Boolean isRealiz) {
        super(name, isRealiz);
    }

    /* End C SPECIFIC HELPERS */

    @Override
    public void facAdd(String name, String concept, String realiz) {
        FacilityDeclBook f;
        f = new CFacilityDeclBook(name, concept, realiz);
        facilityList.add(f);
        currentFacility = f;
    }

    /* FunctionBook Adders */

    @Override
    public void fxnAdd(String retType, String funcName) {
        FunctionBook f;
        f = new CFunctionBook(retType, funcName, isRealization);
        functionList.add(f);
        currentFunction = f;
    }

    @Override
    public String output() {
        StringBuilder output = new StringBuilder();
        output.append("/*").append("Name: ").append(conceptName).append("*/\n");

        output.append(stdimports());
        for (int i = 0; i < facilityList.size(); i++) {
            output.append(facilityList.get(i).getString());
        }
        output.append("\n");
        for (int i = 0; i < functionList.size(); i++) {
            output.append(functionList.get(i).getString());
        }
        output.append("\n").append("/*").append(conceptName).append("*/");

        return output.toString();

    }

    private String stdimports() {
        return "#include Resolve.h \n#include Std_Integer_Realiz.h";
    }
}

class CFacilityDeclBook extends FacilityDeclBook {

    CFacilityDeclBook(String name, String concept, String realiz) {
        super(name, concept, realiz);
    }

    @Override
    String getString() {
        StringBuilder retString = new StringBuilder();
        retString.append("typedef struct ").append(myName).append("{");
        retString.append(myConcept).append("* core;");
        for (FacilityDeclEnhance a : enhanceList) {
            retString.append(a.myName).append("_for_").append(myConcept);
            retString.append("* ").append(a.myName);
        }
        retString.append("} ").append(myName).append(";");
        return retString.toString();
    }
}

class CFunctionBook extends FunctionBook {

    CFunctionBook(String returnType, String name, boolean hasBody) {
        super(returnType, name, hasBody);
    }

    @Override
    String getString() {
        StringBuilder retString = new StringBuilder();
        retString.append(myReturnType).append(" ").append(myName);
        retString.append("(");
        Iterator a = parameterList.iterator();
        /*while(){
            retString.append(a).append(", ");
        }
        this.*/
        return "GETSTRINGFORFUNCTIONBOOK";
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
