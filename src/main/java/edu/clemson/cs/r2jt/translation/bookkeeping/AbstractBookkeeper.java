package edu.clemson.cs.r2jt.translation.bookkeeping;

import edu.clemson.cs.r2jt.translation.bookkeeping.Books.AbstractBook;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

public abstract class AbstractBookkeeper implements Bookkeeper {

    /**
     * <p>Pointers to the various books currently being handled by
     * the Bookkeeper.</p>
     */
    AbstractFunctionBook myCurrentFunction;
    AbstractFacilityDecBook myCurrentFacility;

    /**
     * <p>Name of the module being managed by the Bookkeeper.</p>
     */
    protected String myModuleName;

    /**
     * <p>Flag that indicates to the Bookkeeper whether or not bodies
     * will proceed declarations (I.e. functions, etc).</p>
     */
    protected Boolean isRealization;

    /**
     * <p>These lists can be thought of as the Bookkeeper's shelf -
     * where all completed and current books are stored.</p>
     */
    List<AbstractFacilityDecBook> myFacilityList;
    List<AbstractFunctionBook> myFunctionList;
    List<String> myConstructors;
    List<String> myImportList;

    AbstractBook currentBook;
    List<AbstractBook> myBookList;
    List<String> myInstanceVariables;

    public AbstractBookkeeper(String moduleName, Boolean isRealiz) {
        myModuleName = moduleName;
        isRealization = isRealiz;

        myImportList = new ArrayList();
        myFunctionList = new ArrayList();
        myFacilityList = new ArrayList();
        myBookList = new ArrayList();
        myConstructors = new LinkedList<String>();
    }

    @Override
    public void addUses(String usesName) {
        myImportList.add(usesName);
    }

    @Override
    public void addConstructor(String constructor) {
        myConstructors.add(constructor);
    }

    // -----------------------------------------------------------
    //   FacilityBook methods
    // -----------------------------------------------------------

    public void addBook(Class<AbstractBook> cls, List<String> paramList){
        try{
            Constructor con = cls.getConstructor(List.class);
            if(currentBook == null){
                currentBook = (AbstractBook) con.newInstance(paramList);
                myBookList.add(currentBook);
            } else{
                currentBook.addBook((AbstractBook) con.newInstance(paramList));
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void addVariableDeclaration(String param){
        if(currentBook != null)  currentBook.addElement(param, "vardec");
        else myInstanceVariables.add(param);


    }

    public void addParameter(String param){
        currentBook.addElement(param, "parameter");
    }

    public void appendTo(String param){
        if(currentBook != null)  currentBook.addElement(param, "append");
    }

    public void closeBook(){
        currentBook = null;
    }


    @Override
    public void facAddParameter(String parameter) {
        myCurrentFacility.parameterList.add(parameter);
    }

    @Override
    public void facAddEnhancement(String name, String realiz) {
        try {
            FacilityEnhancementBook enhancement =
                    new FacilityEnhancementBook(name, realiz);
            myCurrentFacility.enhancementList.add(enhancement);
            myCurrentFacility.currentEnhancement = enhancement;
        }
        catch (NullPointerException e) {

        }
    }

    @Override
    public boolean facEnhancementIsOpen() {
        return myCurrentFacility.currentEnhancement != null;
    }

    @Override
    public void facAddEnhancementParameter(String parameter) {
        myCurrentFacility.currentEnhancement.parameterList.add(parameter);
    }

    @Override
    public void facEnhancementEnd() {
        myCurrentFacility.currentEnhancement = null;
    }

    @Override
    public void facEnd() {
        myCurrentFacility = null;
    }

    // -----------------------------------------------------------
    //   FunctionBook methods
    // -----------------------------------------------------------

    @Override
    public void fxnAddParameter(String parameter) {
        myCurrentFunction.parameterList.add(parameter);
    }

    @Override
    public void fxnAddVariableDeclaration(String variable) {
        myCurrentFunction.varInitList.add(variable);
    }

    @Override
    public void fxnAppendTo(String stmt) {
        myCurrentFunction.allStmt.append(stmt);
    }

    @Override
    public boolean fxnIsOpen() {
        return myCurrentFunction != null;
    }

    @Override
    public void fxnEnd() {
        myCurrentFunction = null;
    }
}

abstract class AbstractFunctionBook {

    protected String name;
    protected String returnType;
    protected ArrayList<String> parameterList;
    protected ArrayList<String> varInitList;

    protected StringBuilder allStmt;
    protected boolean isRealization;

    public AbstractFunctionBook(String nameStr, String retStr, boolean isRealiz) {
        name = retStr;
        returnType = nameStr;
        isRealization = isRealiz;

        allStmt = new StringBuilder();
        parameterList = new ArrayList<String>();
        varInitList = new ArrayList<String>();
    }

    abstract String getString();
}

abstract class AbstractFacilityDecBook {

    protected String name;
    protected String concept;
    protected String conceptRealization;

    protected List<FacilityEnhancementBook> enhancementList;
    protected List<String> parameterList;
    protected FacilityEnhancementBook currentEnhancement;

    AbstractFacilityDecBook(String nameStr, String conStr, String realizStr) {
        name = nameStr;
        concept = conStr;
        conceptRealization = realizStr;

        enhancementList = new LinkedList<FacilityEnhancementBook>();
        parameterList = new LinkedList<String>();
    }

    abstract String getString();
}

class FacilityEnhancementBook {

    protected String name;
    protected String realization;
    protected ArrayList<String> parameterList;

    FacilityEnhancementBook(String nameStr, String realizStr) {
        name = nameStr;
        realization = realizStr;
        parameterList = new ArrayList();
    }
}

class RecordBook {

}