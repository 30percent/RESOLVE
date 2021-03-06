/*
 * This software is released under the new BSD 2006 license.
 * 
 * Note the new BSD license is equivalent to the MIT License, except for the
 * no-endorsement final clause.
 * 
 * Copyright (c) 2007, Clemson University
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the Clemson University nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * This sofware has been developed by past and present members of the
 * Reusable Sofware Research Group (RSRG) in the School of Computing at
 * Clemson University. Contributors to the initial version are:
 * 
 * Steven Atkinson
 * Greg Kulczycki
 * Kunal Chopra
 * John Hunt
 * Heather Keown
 * Ben Markle
 * Kim Roche
 * Murali Sitaraman
 */
/*
 * ProgramExpTypeResolver.java
 * 
 * The Resolve Software Composition Workbench Project
 * 
 * Copyright (c) 1999-2005
 * Reusable Software Research Group
 * Department of Computer Science
 * Clemson University
 */

package edu.clemson.cs.r2jt.analysis;

import edu.clemson.cs.r2jt.absyn.*;
import edu.clemson.cs.r2jt.collections.*;
import edu.clemson.cs.r2jt.data.*;
import edu.clemson.cs.r2jt.entry.*;
import edu.clemson.cs.r2jt.errors.ErrorHandler;
import edu.clemson.cs.r2jt.init.CompileEnvironment;
import edu.clemson.cs.r2jt.init.Environment;
import edu.clemson.cs.r2jt.location.OperationLocator;
import edu.clemson.cs.r2jt.location.QualifierLocator;
import edu.clemson.cs.r2jt.location.VariableLocator;
import edu.clemson.cs.r2jt.location.SymbolSearchException;
import edu.clemson.cs.r2jt.scope.Binding;
import edu.clemson.cs.r2jt.scope.ModuleScope;
import edu.clemson.cs.r2jt.scope.OldSymbolTable;
import edu.clemson.cs.r2jt.scope.TypeHolder;
import edu.clemson.cs.r2jt.type.*;

// changed this to add: public OperationEntry
// getOperationEntry(ProgramFunctionExp exp)
public class ProgramExpTypeResolver extends TypeResolutionVisitor {

    // ===========================================================
    // Variables 
    // ===========================================================

    private OldSymbolTable table;

    //private Environment env;
    //private CompileEnvironment myInstanceEnvironment;

    private ErrorHandler err;

    // ===========================================================
    // Constructors
    // ===========================================================

    public ProgramExpTypeResolver(OldSymbolTable table, CompileEnvironment instanceEnvironment) {
        //this.env = new Environment(instanceEnvironment);
        //myInstanceEnvironment = instanceEnvironment;
        this.table = table;
        this.err = instanceEnvironment.getErrorHandler();
    }

    // ===========================================================
    // Public Methods - Variable Checking Methods
    // ===========================================================

    public boolean isVariable(ProgramExp exp) {
        ProgramExp sem = null;
        if (exp instanceof ProgramDotExp) {
            sem = ((ProgramDotExp) exp).getSemanticExp();
        }
        else if (exp instanceof ProgramParamExp) {
            sem = ((ProgramParamExp) exp).getSemanticExp();
        }
        else {
            sem = exp;
        }

        assert sem != null : "sem is null";
        if (sem instanceof VariableExp) {
            return true;
        }
        else {
            return false;
        }
    }

    public void checkReplica(Location loc, Type atype) throws TypeResolutionException {
        /* Variables 
         * - YS */
        OperationLocator locator = new OperationLocator(table, err);
        PosSymbol name = new PosSymbol(loc, Symbol.symbol("Replica"));
        PosSymbol qual = null;

        /* Old code, not needed - YS
        Symbol qualsym = atype.getProgramName().getFacilityQualifier();

        assert qualsym != null : "qualsym is null";

        PosSymbol  qual = null;
        if(qualsym != null) qual = new PosSymbol(loc, qualsym);*/

        /* Find the type of the array 
         * - YS */
        atype = getArrayType(loc, atype);
        if (atype instanceof IndirectType) {
            /* We got something of type Entry, 
             * we need to know what type is it exactly 
             * - YS */
            atype = ((IndirectType) atype).getType();
        }

        /* Add the type to the arg type list - YS */
        List<Type> args = new List<Type>();
        args.add(atype);

        try {
            OperationEntry oper = locator.locateOperation(qual, name, args);
        }
        catch (SymbolSearchException ex) {
            throw new TypeResolutionException();
        }
    }

    // ===========================================================
    // Public Methods - Abstract Visit Methods
    // ===========================================================

    public Type getProgramExpType(ProgramExp exp) throws TypeResolutionException {
        Type result;

        if (exp.getMathType() != null) {
            result = new NewProgramType(exp.getProgramType());
        }
        else {
            result = exp.accept(this);
        }

        return result;
    }

    public Type getVariableExpType(VariableExp exp) throws TypeResolutionException {
        return exp.accept(this);
    }

    // ===========================================================
    // Public Methods - Program Expressions
    // ===========================================================

    public Type getProgramOpExpType(ProgramOpExp exp) throws TypeResolutionException {
        // create program function name
        String str = null;
        switch (exp.getOperator()) {
        case ProgramOpExp.AND:
            str = "And";
            break;
        case ProgramOpExp.OR:
            str = "Or";
            break;
        case ProgramOpExp.EQUAL:
            str = "Are_Equal";
            break;
        case ProgramOpExp.NOT_EQUAL:
            str = "Are_Not_Equal";
            break;
        case ProgramOpExp.LT:
            str = "Less";
            break;
        case ProgramOpExp.LT_EQL:
            str = "Less_Or_Equal";
            break;
        case ProgramOpExp.GT:
            str = "Greater";
            break;
        case ProgramOpExp.GT_EQL:
            str = "Greater_Or_Equal";
            break;
        case ProgramOpExp.PLUS:
            str = "Sum";
            break;
        case ProgramOpExp.MINUS:
            str = "Difference";
            break;
        case ProgramOpExp.MULTIPLY:
            str = "Product";
            break;
        case ProgramOpExp.DIVIDE:
            str = "Quotient";
            break;
        case ProgramOpExp.REM:
            str = "Rem";
            break;
        case ProgramOpExp.MOD:
            str = "Mod";
            break;
        case ProgramOpExp.DIV:
            str = "Div";
            break;
        case ProgramOpExp.EXP:
            str = "Power";
            break;
        case ProgramOpExp.NOT:
            str = "Not";
            break;
        case ProgramOpExp.UNARY_MINUS:
            str = "Negate";
            break;

        default:
            assert false : "Invalid Operator: " + exp.getOperator();
            break;
        }
        PosSymbol name = new PosSymbol(exp.getLocation(), Symbol.symbol(str));
        // create program function argument list
        List<ProgramExp> arguments = new List<ProgramExp>();
        arguments.add(exp.getFirst());
        if (exp.getSecond() != null) {
            arguments.add(exp.getSecond());
        }
        // create program function exp
        ProgramFunctionExp func = new ProgramFunctionExp(exp.getLocation(), null, name, arguments);
        return getProgramFunctionExpType(func);
    }

    public Type getProgramDotExpType(ProgramDotExp exp) throws TypeResolutionException {
        if (exp.getSemanticExp() == null) {
            exp.setSemanticExp(extractSemanticExp(exp));
        }
        return getProgramExpType(exp.getSemanticExp());
    }

    public Type getProgramParamExpType(ProgramParamExp exp) throws TypeResolutionException {
        /* Checks for if the function operation exists before we call it
         * Note: This may not be the place that we throw this exception. - YS
         */
        try {

            if (exp.getSemanticExp() == null) {
                exp.setSemanticExp(extractSemanticExp(exp));
                exp.getSemanticExp().setType(getProgramExpType(exp.getSemanticExp()));
            }

            return getProgramExpType(exp.getSemanticExp());
        }
        catch (TypeResolutionException ex) {
            String msg;
            if (exp.getName().getName().equals("Replica")) {
                msg = "Cannot locate a Replica operation for this variable";
            }
            else {
                msg = "Cannot locate an operation with this name: " + exp.getName().getName();
            }
            err.error(exp.getLocation(), msg);
            throw new TypeResolutionException();
        }
    }

    public Type getProgramFunctionExpType(ProgramFunctionExp exp) throws TypeResolutionException {
        try {
            List<Type> argtypes = new List<Type>();
            Iterator<ProgramExp> i = exp.getArguments().iterator();
            while (i.hasNext()) {
                ProgramExp argexp = i.next();
                Type argtype = getProgramExpType(argexp);
                argtypes.add(argtype);
            }

            OperationLocator locator = new OperationLocator(table, err); //Problem's here
            if (exp.getName().getSymbol().getName().equals("LessThanOrEqual")) {
                System.out.println("ProgramExpTypeResolver.getProgramFunctionExpType");
            }
            OperationEntry oper = locator.locateOperation(exp.getQualifier(), exp.getName(), argtypes);
            return oper.getType();
        }
        catch (SymbolSearchException ex) {
            throw new TypeResolutionException();
        }
    }

    // Moved this from *.translation.ProgramExpTypeResolver.java to here and removed 
    // that file altogether, because this operation seems to be the only difference between the two replicated files!
    // Also remove the correspondponding file from verification package.
    public OperationEntry getOperationEntry(ProgramFunctionExp exp) throws TypeResolutionException {
        try {
            List<Type> argtypes = new List<Type>();
            if (exp.getArguments() != null) {
                Iterator<ProgramExp> i = exp.getArguments().iterator();
                while (i.hasNext()) {
                    ProgramExp argexp = i.next();
                    Type argtype = getProgramExpType(argexp);
                    argtypes.add(argtype);
                }
            }
            OperationLocator locator = new OperationLocator(table, err);
            OperationEntry oper = locator.locateOperation(exp.getQualifier(), exp.getName(), argtypes);
            return oper;
        }
        catch (SymbolSearchException ex) {
            throw new TypeResolutionException();
        }
    }

    public Type getProgramIntegerExpType(ProgramIntegerExp exp) throws TypeResolutionException {
        TypeHolder holder = table.getTypeHolder();
        if (holder.containsTypeInteger()) {
            return holder.getTypeInteger();
        }
        else {
            String msg = cantFindType("Std_Integer_Fac.Integer");
            err.error(exp.getLocation(), msg);
            throw new TypeResolutionException();
        }
    }

    public Type getProgramCharExpType(ProgramCharExp exp) throws TypeResolutionException {
        TypeHolder holder = table.getTypeHolder();
        if (holder.containsTypeChar()) {
            return holder.getTypeChar();
        }
        else {
            String msg = cantFindType("Std_Character_Fac.Character"); //Changed this from Char to Character
            err.error(exp.getLocation(), msg);
            throw new TypeResolutionException();
        }
    }

    public Type getProgramStringExpType(ProgramStringExp exp) throws TypeResolutionException {
        TypeHolder holder = table.getTypeHolder();
        if (holder.containsTypeChar_Str()) {
            return holder.getTypeChar_Str();
        }
        else {
            String msg = cantFindType("Std_Char_Str_Fac.Char_Str");
            err.error(exp.getLocation(), msg);
            throw new TypeResolutionException();
        }
    }

    public Type getProgramDoubleExpType(ProgramDoubleExp exp) throws TypeResolutionException {
        TypeHolder holder = table.getTypeHolder();
        if (holder.containsTypeReal()) {
            return holder.getTypeReal();
        }
        else {
            String msg = cantFindType("Std_Real_Number_Fac.Real");
            err.error(exp.getLocation(), msg);
            throw new TypeResolutionException();
        }
    }

    public Type getVariableDotExpType(VariableDotExp exp) throws TypeResolutionException {
        if (exp.getSemanticExp() == null) {
            exp.setSemanticExp(extractSemanticExp(exp));
        }
        return getProgramExpType(exp.getSemanticExp());
    }

    public Type getVariableNameExpType(VariableNameExp exp) throws TypeResolutionException {
        VariableLocator locator = new VariableLocator(table, err);
        try {
            VarEntry entry = locator.locateProgramVariable(exp.getQualifier(), exp.getName());
            return entry.getType();
        }
        catch (SymbolSearchException ex) {
            throw new TypeResolutionException();
        }
    }

    public Type getVariableArrayExpType(VariableArrayExp exp) throws TypeResolutionException {
        VariableLocator locator = new VariableLocator(table, err);
        try {
            VarEntry entry = locator.locateProgramVariable(exp.getQualifier(), exp.getName());

            /* The preprocessor changes all ArrayTy to a Facility, so we will when we try to check the type of entry,
             * it will never be of ArrayType 
             * - YS
            checkArrayType(exp, entry.getType());
            //checkArrayIndex(exp.getArgument());
            ArrayType arrtype = castToArrayType(entry.getType());
            return arrtype.getEntry();
             */

            return entry.getType();
        }
        catch (SymbolSearchException ex) {
            throw new TypeResolutionException();
        }
    }

    public Type getVariableRecordExpType(VariableRecordExp exp) throws TypeResolutionException {
        try {
            VariableLocator locator = new VariableLocator(table, err);
            VarEntry entry = locator.locateProgramVariable(exp.getQualifier(), exp.getName());
            Type type = entry.getType();
            Type fieldtype = null;
            Iterator<VariableExp> i = exp.getFields().iterator();
            while (i.hasNext()) {
                VariableExp field = i.next();
                checkRecordType(exp, type);
                RecordType rectype = castToRecordType(type);
                fieldtype = getFieldType(rectype, field);
                if (i.hasNext()) {
                    type = fieldtype;
                }
            }
            return fieldtype;
        }
        catch (SymbolSearchException ex) {
            throw new TypeResolutionException();
        }
    }

    // ===========================================================
    // Private Methods - Semantic Expression Extraction
    // ===========================================================

    private VariableExp extractSemanticExp(VariableDotExp exp) throws TypeResolutionException {
        List<ProgramExp> segs = new List<ProgramExp>();
        Iterator<VariableExp> i = exp.getSegments().iterator();
        while (i.hasNext()) {
            segs.add((ProgramExp) i.next());
        }
        ProgramDotExp dexp = new ProgramDotExp(exp.getLocation(), segs, exp.getSemanticExp());
        ProgramExp pexp = extractSemanticExp(dexp);
        return castToVariableExp(pexp);
    }

    private ProgramExp extractSemanticExp(ProgramDotExp exp) throws TypeResolutionException {
        ProgramExp exp1 = exp.getSegments().get(0);
        ProgramExp exp2 = exp.getSegments().get(1);
        if (isProgramQualifier(exp1)) {
            PosSymbol qual = getProgramQualifier(exp1);
            if (exp.getSegments().size() == 2) {
                if (exp2 instanceof VariableNameExp) {
                    return getQualifiedName(qual, (VariableNameExp) exp2);
                }
                else if (exp2 instanceof VariableArrayExp) {
                    return getQualifiedArray(qual, (VariableArrayExp) exp2);
                }
                else if (exp2 instanceof ProgramParamExp) {
                    return extractSemanticExp(qual, (ProgramParamExp) exp2);
                }
                else {
                    assert false : "exp2 is an invalid expression type";
                }
            }
            else { // more than 2 segments
                return convertToRecord(qual, exp);
            }
        }
        return convertToRecord(exp);
    }

    private ProgramExp extractSemanticExp(ProgramParamExp exp) throws TypeResolutionException {
        return extractSemanticExp(null, exp);
    }

    //      private ProgramExp extractSemanticExp(PosSymbol qual, ProgramParamExp exp)
    //          throws TypeResolutionException
    //      {
    //          try {
    //              VariableLocator locator = new VariableLocator(table);
    //              if (locator.isArrayVariable(qual, exp.getName())) {
    //                  if (exp.getArguments().size() > 1) {
    //                      String msg = multArgsInArrayMessage();
    //                      err.error(exp.getLocation(), msg);
    //                      throw new TypeResolutionException();
    //                  }
    //                  return new VariableArrayExp(exp.getLocation(), qual,
    //                                 exp.getName(), exp.getArguments().get(0));
    //              } else {
    //                  return new ProgramFunctionExp(exp.getLocation(), qual,
    //                                 exp.getName(), exp.getArguments());
    //              }
    //          } catch (SymbolSearchException ex) {
    //              throw new TypeResolutionException();
    //          }
    //      }

    private ProgramExp extractSemanticExp(PosSymbol qual, ProgramParamExp exp) throws TypeResolutionException {
        //          try {
        VariableLocator locator = new VariableLocator(table, err);
        if (exp.getArguments().size() == 1 && locator.isArrayVariable(qual, exp.getName())) {
            try {
                checkArrayIndex(exp.getArguments().get(0));
            }
            catch (TypeResolutionException trex) {
                return new ProgramFunctionExp(exp.getLocation(), qual, exp.getName(), exp.getArguments());
            }
            return new VariableArrayExp(exp.getLocation(), qual, exp.getName(), exp.getArguments().get(0));
        }
        else {
            ProgramFunctionExp result =
                    new ProgramFunctionExp(exp.getLocation(), qual, exp.getName(), exp.getArguments());
            result.setMathType(exp.getMathType());
            result.setMathTypeValue(exp.getMathTypeValue());
            result.setProgramType(exp.getProgramType());
            return result;
        }
        //          } catch (SymbolSearchException ex) {
        //              throw new TypeResolutionException();
        //          }
    }

    private VariableNameExp getQualifiedName(PosSymbol qual, VariableNameExp exp) {
        return new VariableNameExp(exp.getLocation(), qual, exp.getName());
    }

    private VariableArrayExp getQualifiedArray(PosSymbol qual, VariableArrayExp exp) {
        return new VariableArrayExp(exp.getLocation(), qual, exp.getName(), exp.getArgument());
    }

    private VariableArrayExp convertToArray(ProgramParamExp exp) throws TypeResolutionException {
        if (exp.getArguments().size() > 1) {
            String msg = multArgsInArrayMessage();
            err.error(exp.getLocation(), msg);
            throw new TypeResolutionException();
        }
        return new VariableArrayExp(exp.getLocation(), null, exp.getName(), exp.getArguments().get(0));
    }

    private VariableRecordExp convertToRecord(ProgramDotExp exp) throws TypeResolutionException {
        return convertToRecord(null, exp);
    }

    private VariableRecordExp convertToRecord(PosSymbol qual, ProgramDotExp exp)
            throws TypeResolutionException {
        VariableNameExp nexp = null;
        int start = 0;
        if (qual != null) {
            nexp = castToVariableNameExp(exp.getSegments().get(1));
            start = 2;
        }
        else {
            nexp = castToVariableNameExp(exp.getSegments().get(0));
            start = 1;
        }
        PosSymbol name = nexp.getName();
        List<VariableExp> segs = new List<VariableExp>();
        for (int i = start; i < exp.getSegments().size(); i++) {
            ProgramExp pexp = exp.getSegments().get(i);
            VariableExp vexp = null;
            if (pexp instanceof ProgramParamExp) {
                vexp = convertToArray((ProgramParamExp) pexp);
            }
            else {
                vexp = castToVariableExp(pexp);
            }
            segs.add(vexp);
        }
        Location loc = (qual == null) ? exp.getLocation() : qual.getLocation();
        return new VariableRecordExp(loc, qual, name, segs);
    }

    private boolean isProgramQualifier(ProgramExp exp) {
        if (exp instanceof VariableNameExp) {
            PosSymbol qual = ((VariableNameExp) exp).getName();
            QualifierLocator locator = new QualifierLocator(table, err);
            return locator.isProgramQualifier(qual);
        }
        return false;
    }

    private PosSymbol getProgramQualifier(ProgramExp exp) {
        if (exp instanceof VariableNameExp) {
            return ((VariableNameExp) exp).getName();
        }
        assert false : "exp is not an instance of VariableNameExp";
        return null;
    }

    private void checkArrayType(VariableExp exp, Type type) throws TypeResolutionException {
        while (type instanceof IndirectType) {
            type = ((IndirectType) type).getType();
        }
        if (!(type instanceof ArrayType)) {
            String msg = expectingArrayMessage(type.toString());
            err.error(exp.getLocation(), msg);
            throw new TypeResolutionException();
        }
    }

    private void checkArrayIndex(ProgramExp exp) throws TypeResolutionException {
        Type type = getProgramExpType(exp);
        TypeHolder holder = table.getTypeHolder();
        if (holder.containsTypeInteger()) {
            Type itype = holder.getTypeInteger();
            TypeMatcher matcher = new TypeMatcher();
            if (!matcher.programMatches(type, itype)) {
                String msg =
                        expectedDiffTypeMessage(itype.getRelativeName(exp.getLocation()), type
                                .getRelativeName(exp.getLocation()));
                err.error(exp.getLocation(), msg);
                throw new TypeResolutionException();
            }
        }
        else {
            String msg = cantFindType("Std_Integer_Fac.Integer");
            err.error(exp.getLocation(), msg);
            throw new TypeResolutionException();
        }
    }

    private void checkRecordType(VariableExp exp, Type type) throws TypeResolutionException {
        //NOTE: Try establishing the progress metric for this loop!
        while (type instanceof IndirectType) {
            type = ((IndirectType) type).getType();
        }
        if (!(type instanceof RecordType)) {
            String msg;
            String progTypeName = type.getRelativeName(exp.getLocation());
            if (type instanceof ArrayType) {
                msg = expectingRecordFoundArrayMessage(progTypeName);
            }
            else {
                msg = expectingRecordFoundHiddenMessage(progTypeName);
            }
            err.error(exp.getLocation(), msg);
            throw new TypeResolutionException();
        }
    }

    private Type getFieldType(RecordType rectype, VariableExp field) throws TypeResolutionException {
        PosSymbol name = null;
        if (field instanceof VariableNameExp) {
            name = ((VariableNameExp) field).getName();
        }
        else if (field instanceof VariableArrayExp) {
            name = ((VariableArrayExp) field).getName();
            checkArrayIndex(((VariableArrayExp) field).getArgument());
        }
        else {
            assert false : "field is an invalid type.";
            throw new TypeResolutionException();
        }
        Iterator<FieldItem> i = rectype.getFields().iterator();
        while (i.hasNext()) {
            FieldItem item = i.next();
            if (item.getName().getSymbol() == name.getSymbol()) {
                if (item.getType() instanceof ArrayType && field instanceof VariableArrayExp) {
                    return ((ArrayType) item.getType()).getEntry();
                }
                else {
                    return item.getType();
                }
            }
        }
        String msg = fieldNotFoundMessage(name.toString(), rectype.toString());
        err.error(name.getLocation(), msg);
        throw new TypeResolutionException();
    }

    // -----------------------------------------------------------
    // Cast Methods
    // -----------------------------------------------------------

    private ArrayType castToArrayType(Type type) {
        if (type instanceof IndirectType) {
            type = ((IndirectType) type).getType();
        }
        if (type instanceof ArrayType) {
            return (ArrayType) type;
        }
        else {
            assert false : "type is invalid";
            return null;
        }
    }

    private RecordType castToRecordType(Type type) {
        if (type instanceof IndirectType) {
            type = ((IndirectType) type).getType();
        }
        if (type instanceof RecordType) {
            return (RecordType) type;
        }
        else {
            assert false : "type is invalid";
            return null;
        }
    }

    private VariableNameExp castToVariableNameExp(ProgramExp exp) {
        if (exp instanceof VariableNameExp) {
            return (VariableNameExp) exp;
        }
        else {
            assert false : "exp is invalid";
            return null;
        }
    }

    private VariableExp castToVariableExp(ProgramExp exp) {
        if (exp instanceof VariableExp) {
            return (VariableExp) exp;
        }
        else {
            assert false : "exp is invalid";
            return null;
        }
    }

    // -----------------------------------------------------------
    // Array Facility Type Methods
    // -----------------------------------------------------------

    /* Finds the type of the array */
    private Type getArrayType(Location loc, Type type) throws TypeResolutionException {
        /* Gets rid of all the IndirectType wrappers */
        while (type instanceof IndirectType) {
            type = ((IndirectType) type).getType();
        }

        /* Check if it is a NameType containing a FunctionType */
        if (type instanceof NameType && ((NameType) type).getType() instanceof FunctionType) {
            FunctionType fType = (FunctionType) ((NameType) type).getType();
            return fType.getRange();
        }
        else {
            String msg = expectingArrayMessage(type.toString());
            err.error(loc, msg);
            throw new TypeResolutionException();
        }
    }

    // -----------------------------------------------------------
    // Error Related Methods
    // -----------------------------------------------------------

    private String cantFindType(String name) {
        return "The type " + name + " is not visible from this module.";
    }

    private String expectedDiffTypeMessage(String t1, String t2) {
        return "  Expected type: " + t1 + "\n" + "  Found type: " + t2;
    }

    private String multArgsInArrayMessage() {
        return "Cannot have multiple arguments in array.";
    }

    private String expectingArrayMessage(String type) {
        return "Expecting an array, but found type " + type + ".";
    }

    private String expectingRecordFoundArrayMessage(String type) {
        return "  Expected a type represented by a record" + "\n" + "  Found type: " + type
                + " represented by an array.";
    }

    private String expectingRecordFoundHiddenMessage(String type) {
        return "  Expected a type represented by a record" + "\n" + "  Found type: " + type
                + " whose representation is hidden.";
    }

    private String fieldNotFoundMessage(String field, String record) {
        return "Could not find a field with name " + field + " in record " + record + ".";
    }
}
