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
 * Populator.java
 * 
 * The Resolve Software Composition Workbench Project
 * 
 * Copyright (c) 1999-2005
 * Reusable Software Research Group
 * Department of Computer Science
 * Clemson University
 */

package edu.clemson.cs.r2jt.population;

import edu.clemson.cs.r2jt.absyn.*;
import edu.clemson.cs.r2jt.collections.*;
import edu.clemson.cs.r2jt.data.*;
import edu.clemson.cs.r2jt.entry.*;
import edu.clemson.cs.r2jt.errors.*;
import edu.clemson.cs.r2jt.init.CompileEnvironment;
import edu.clemson.cs.r2jt.init.Environment;
import edu.clemson.cs.r2jt.init.ImportScanner;
import edu.clemson.cs.r2jt.location.TypeLocator;
import edu.clemson.cs.r2jt.scope.Binding;
import edu.clemson.cs.r2jt.scope.ModuleScope;
import edu.clemson.cs.r2jt.scope.OldSymbolTable;
import edu.clemson.cs.r2jt.scope.TypeID;
import edu.clemson.cs.r2jt.type.*;

/**
 * <p>
 * The purpose of the <code>Populator</code> is to take a
 * <code>SymbolTable</code> and traverse the abstract syntax tree, filling in
 * the <code>SymbolTable</code> with information about variables and their
 * types.</code>
 */
public class OldPopulator extends ResolveConceptualVisitor {

    // ===========================================================
    // Variables
    // ===========================================================

    private OldSymbolTable table;

    //private Environment env = Environment.getInstance();

    private ErrorHandler err;

    private CompileEnvironment myInstanceEnvironment;

    // =======================s====================================
    // Constructors
    // ===========================================================

    public OldPopulator(OldSymbolTable table, CompileEnvironment myInstanceEnvironment) {
        this.table = table;
        this.myInstanceEnvironment = myInstanceEnvironment;
        this.err = myInstanceEnvironment.getErrorHandler();
    }

    // ===========================================================
    // Public Methods - Abstract Visit Methods
    // ===========================================================

    public void visitModuleDec(ModuleDec dec) {

        // TODO : All of the visitXModuleDec methods end with
        // table.completeModuleScope except one. Can this be changed so
        // that completeModuleScope can be factored out as well? -HwS
        table.createModuleScope();

        // This procedure determines whether the dec is a Std_Fac or one of its
        // dependencies and if not adds the Std_Fac to the table's scope
        // NOTE: If you are adding more Std_Facs they should be added to the stdUses array in CompileEnvironment
        // TODO: This mechanism is a duplicate of that in the
        // ImportScanner.visitModuleDec(), perhaps combine these somehow? -JCK
        String[] stdUses = myInstanceEnvironment.getStdUses();
        String decName = dec.getName().getName();

        if (!ImportScanner.NO_DEFAULT_IMPORT_MODULES.contains(decName)) {

            List<List<UsesItem>> listOfDependLists = myInstanceEnvironment.getStdUsesDepends();

            for (int i = 0; i < stdUses.length; i++) {
                if (decName.equals("Std_" + stdUses[i] + "_Fac") || decName.equals(stdUses[i] + "_Template")
                        || decName.equals(stdUses[i] + "_Theory")) {
                    // Don't need to do anything for these
                    dec.accept(this);
                    return;
                }
                else {
                    // Check if this Dec is a dependency
                    if (!listOfDependLists.isEmpty()) {
                        List<UsesItem> dependencies = listOfDependLists.get(i);
                        if (dependencies != null) {
                            Iterator<UsesItem> it = dependencies.iterator();
                            while (it.hasNext()) {
                                if (it.next().getName().getName().equals(decName)) {
                                    // This is a dependency do NOT add
                                    dec.accept(this);
                                    return;
                                }
                            }
                        }
                    }
                    // Not a Std_Fac or dependency; Add the Fac to the table scope
                    Symbol stdFac;
                    stdFac = Symbol.symbol("Std_" + stdUses[i] + "_Fac");

                    PosSymbol stdFacPosSymbol = new PosSymbol(null, stdFac);
                    ModuleID stdBooleanFacModuleID = ModuleID.createFacilityID(stdFac);
                    ModuleEntry entry =
                            new ModuleEntry(stdFacPosSymbol, myInstanceEnvironment.getSymbolTable(
                                    stdBooleanFacModuleID).getModuleScope());
                    table.addFacilityToScope(entry);
                }
            }
        }

        dec.accept(this);
    }

    public void visitDec(Dec dec) {
        dec.accept(this);
    }

    public void visitStatement(Statement stmt) {
        stmt.accept(this);
    }

    /*
     * Note: For the time being, we are not analyzing math expressions. This
     * means that we cannot populate math expression either, since the scope
     * handling depends upon the populator and the analyzer being synchronized
     * in the way they recurse through the module dec (and therefore in the
     * scopes they visit). In short, if a scope is created in the populator but
     * not visited in the analyzer, everything get screwed up.
     */
    public void visitExp(Exp exp) {
        // DEBUG
        // return;
        if (exp == null) {
            return;
        }
        exp.accept(this);
    }

    // ===========================================================
    // Public Methods - Declarations
    // ===========================================================

    // -----------------------------------------------------------
    // Module Declarations
    // -----------------------------------------------------------

    public void visitMathModuleDec(MathModuleDec dec) {
        visitUsesItemList(dec.getUsesItems());
        visitModuleParameterList(dec.getParameters());
        visitDecList(dec.getDecs());
        table.completeModuleScope();
    }

    public void visitProofModuleDec(ProofModuleDec dec) {
        visitUsesItemList(dec.getUsesItems());
        visitModuleParameterList(dec.getModuleParams());
        visitDecList(dec.getDecs());
        table.completeModuleScope();
    }

    public void visitConceptModuleDec(ConceptModuleDec dec) {
        visitUsesItemList(dec.getUsesItems());
        visitModuleParameterList(dec.getParameters());
        visitExp(dec.getRequirement());
        visitExpList(dec.getConstraints());
        if (dec.getFacilityInit() != null) {
            visitInitItem(dec.getFacilityInit());
        }
        if (dec.getFacilityFinal() != null) {
            visitFinalItem(dec.getFacilityFinal());
        }
        visitDecList(dec.getDecs());
        table.completeModuleScope();
    }

    public void visitEnhancementModuleDec(EnhancementModuleDec dec) {
        visitUsesItemList(dec.getUsesItems());
        table.addAssocConcept(dec.getConceptName());
        table.addAssocVisibleModules();
        visitModuleParameterList(dec.getParameters());
        visitExp(dec.getRequirement());
        visitDecList(dec.getDecs());
        table.completeModuleScope();
    }

    public void visitConceptBodyModuleDec(ConceptBodyModuleDec dec) {
        visitUsesItemList(dec.getUsesItems());
        table.addConceptSpec(dec.getConceptName());
        Iterator<PosSymbol> i = dec.getEnhancementNames().iterator();
        while (i.hasNext()) {
            table.addEnhancementSpec(i.next(), dec.getConceptName());
        }
        table.addAssocVisibleModules();
        visitModuleParameterList(dec.getParameters());
        visitExp(dec.getRequires());
        visitExpList(dec.getConventions());
        visitExpList(dec.getCorrs());
        if (dec.getFacilityInit() != null) {
            visitInitItem(dec.getFacilityInit());
        }
        if (dec.getFacilityFinal() != null) {
            visitFinalItem(dec.getFacilityFinal());
        }
        visitDecList(dec.getDecs());
        table.completeModuleScope();
    }

    public void visitEnhancementBodyModuleDec(EnhancementBodyModuleDec dec) {
        visitUsesItemList(dec.getUsesItems());
        table.addEnhancementSpec(dec.getEnhancementName(), dec.getConceptName());
        table.addAssocConcept(dec.getConceptName());
        addAssocEnhancementsToTable(dec.getEnhancementBodies(), dec.getConceptName());
        table.addAssocVisibleModules();
        visitModuleParameterList(dec.getParameters());
        visitExp(dec.getRequires());
        visitExpList(dec.getConventions());
        visitExpList(dec.getCorrs());
        if (dec.getFacilityInit() != null) {
            visitInitItem(dec.getFacilityInit());
        }
        if (dec.getFacilityFinal() != null) {
            visitFinalItem(dec.getFacilityFinal());
        }
        visitDecList(dec.getDecs());
        table.completeModuleScope();
    }

    public void visitFacilityModuleDec(FacilityModuleDec dec) {
        visitUsesItemList(dec.getUsesItems());
        if (dec.getFacilityInit() != null) {
            visitInitItem(dec.getFacilityInit());
        }
        if (dec.getFacilityFinal() != null) {
            visitFinalItem(dec.getFacilityFinal());
        }
        visitDecList(dec.getDecs());
        table.completeModuleScope();
    }

    public void visitShortFacilityModuleDec(ShortFacilityModuleDec dec) {
        visitUsesItemList(dec.getUsesItems());
        visitFacilityDec(dec.getDec());
        table.completeModuleScope();
        table.createShortFacility(dec.getName());
    }

    // -----------------------------------------------------------
    // Variable Declarations
    // -----------------------------------------------------------

    /* Operation parameters */
    public void visitParameterVarDec(ParameterVarDec dec) {
        VarEntry var =
                new VarEntry(table.getCurrentScope(), dec.getMode(), dec.getName(), getProgramType(dec
                        .getTy()));
        table.addVariableToScope(var);
    }

    // -----------------------------------------------------------
    // Math Declarations
    // -----------------------------------------------------------

    private boolean isDecAVar(DefinitionDec dec) {
        List<MathVarDec> params1 = dec.getParameters();
        if (params1 == null)
            return false;
        Iterator<MathVarDec> i = params1.iterator();
        if (!i.hasNext() && !(getMathType(dec.getReturnTy()) instanceof FunctionType)) {
            if (dec.getDefinition() == null && dec.getBase() == null && dec.getHypothesis() == null) {
                return true;
            }
        }
        return false;
    }

    public void visitDefinitionDec(DefinitionDec dec) {
        // System.out.println("visitDefinitionDec: " + dec.asString(0, 5));

        // If the defn. has no params, treat it as a VarEntry
        if (isDecAVar(dec)) {
            VarEntry vEntry =
                    new VarEntry(table.getCurrentScope(), Mode.MATH, dec.getName(), getMathType(dec
                            .getReturnTy()));
            table.addVariableToScope(vEntry);
        }
        // "Definition suc: N -> N;", etc.
        else if (dec.getReturnTy() instanceof FunctionTy) {
            // Add as a definition
            DefinitionEntry defEntry = null;
            FunctionType ftype = (FunctionType) (getMathType(dec.getReturnTy()));
            Type newParam = ftype.getDomain();
            Type retValue = ftype.getRange();
            PosSymbol ps = new PosSymbol(null, Symbol.symbol(""));
            // "Definition conj: B x B -> B;"
            if (newParam instanceof TupleType) {
                List<FieldItem> oldParams = ((TupleType) newParam).getFields();
                Iterator<FieldItem> i = oldParams.iterator();
                List<VarEntry> newParamList = new List<VarEntry>();
                while (i.hasNext()) {
                    VarEntry ve = new VarEntry(table.getCurrentScope(), Mode.MATH, ps, i.next().getType());
                    newParamList.add(ve);
                }
                defEntry =
                        new DefinitionEntry(table.getCurrentScope(), dec.getName(), newParamList, retValue);
                table.addDefinitionToScope(defEntry);
            }
            else {
                VarEntry vEntry = new VarEntry(table.getCurrentScope(), Mode.MATH, ps, newParam);
                List<VarEntry> params = new List<VarEntry>();
                params.add(vEntry);
                defEntry = new DefinitionEntry(table.getCurrentScope(), dec.getName(), params, retValue);
                table.addDefinitionToScope(defEntry);
            }
        }
        // "Definition suc(x: N): N = ...;"
        else {
            DefinitionEntry defEntry = null;
            List<VarEntry> params = new List<VarEntry>();
            if (dec.getParameters() != null) {
                Iterator<MathVarDec> paramsIt = dec.getParameters().iterator();
                while (paramsIt.hasNext()) {
                    MathVarDec mvDec = paramsIt.next();
                    VarEntry vEntry =
                            new VarEntry(table.getCurrentScope(), Mode.MATH, mvDec.getName(),
                                    getMathType(mvDec.getTy()));
                    params.add(vEntry);
                }
            }

            defEntry =
                    new DefinitionEntry(table.getCurrentScope(), dec.getName(), params, getMathType(dec
                            .getReturnTy()));
            table.addDefinitionToScope(defEntry);
        }
        addDefinitionAsType(dec);
        table.createDefinitionScope(dec.getName());
        visitDefinitionParameters(dec.getParameters());
        visitExp(dec.getBase());
        visitExp(dec.getHypothesis());
        visitExp(dec.getDefinition());
        table.completeDefinitionScope();
    }

    private Type getTypeOfSetType(Type t) {
        if (t instanceof ConstructedType) {
            ConstructedType c = (ConstructedType) t;
            if (c.getName().getName().equalsIgnoreCase("set")) {
                List<Type> args = c.getArgs();
                if (args.size() == 1) {
                    return args.get(0);
                }
            }
        }
        return null;
    }

    private void addDefinitionAsType(DefinitionDec dec) {
        TypeConverter tc = new TypeConverter(table);
        Type t = tc.getMathType(dec.getReturnTy());
        Type typeOfSet = getTypeOfSetType(t);
        if (typeOfSet != null) {
            MathVarDec vd = null;
            Exp w = null;
            Exp o = null;
            if (dec.getDefinition() instanceof SetExp) {
                SetExp se = (SetExp) (dec.getDefinition());
                vd = se.getVar();
                w = se.getWhere();
                o = se.getBody();
            }
            TypeEntry te = new TypeEntry(table.getCurrentScope(), dec.getName(), typeOfSet, vd, w, o);
            table.addDefinitionTypeToScope(te);
        }
    }

    public void visitMathAssertionDec(MathAssertionDec dec) {
        TheoremEntry entry = new TheoremEntry(dec.getName(), dec.getKind());
        entry.setValue(dec.getAssertion());
        table.addTheoremToScope(entry);
        assert dec.getAssertion() != null : "Assertion is null";
        table.createExpressionScope();
        visitExp(dec.getAssertion());
        table.completeExpressionScope();
    }

    public void visitMathTypeDec(MathTypeDec dec) {
        Type decType;

        if (dec.getTy() instanceof FunctionTy) {
            int paramCount = 0;
            FunctionTy ty = (FunctionTy) dec.getTy();
            if (ty.getDomain() instanceof TupleTy) {
                paramCount = ((TupleTy) ty.getDomain()).getFields().size();
            }
            else {
                paramCount = 1;
            }

            decType = new PrimitiveType(table.getModuleID(), dec.getName(), paramCount);
        }
        else {
            decType = new PrimitiveType(table.getModuleID(), dec.getName(), 0);
        }

        TypeEntry entry = new TypeEntry(table.getCurrentScope(), dec.getName(), decType);
        table.addTypeToScope(entry);
    }

    public void visitMathTypeFormalDec(MathTypeFormalDec dec) {
        Type type = new MathFormalType(table.getModuleID(), dec.getName());
        TypeEntry entry = new TypeEntry(table.getCurrentScope(), dec.getName(), type);
        table.addTypeToScope(entry);
    }

    public void visitSubtypeDec(SubtypeDec dec) {
        TypeID tid1 = new TypeID(dec.getQualifier1(), dec.getName1(), 0);
        TypeID tid2 = new TypeID(dec.getQualifier2(), dec.getName2(), 0);
        makeTypeCorrespondence(tid1, tid2);
    }

    private void makeTypeCorrespondence(TypeID base, TypeID subtype) {
        TypeLocator tl = new TypeLocator(table, myInstanceEnvironment);

        try {
            TypeEntry baseEntry = tl.locateMathType(base);
            TypeEntry subtypeEntry = tl.locateMathType(subtype);
            table.addTypeCorrespondence(baseEntry.getType(), subtypeEntry.getType());
        }
        catch (Exception e) {
            ;
        }
    }

    // -----------------------------------------------------------
    // Type Declarations
    // -----------------------------------------------------------

    public void visitFacilityTypeDec(FacilityTypeDec dec) {
        Type type = getProgramType(dec.getRepresentation(), dec.getName());
        TypeEntry entry = new TypeEntry(table.getCurrentScope(), dec.getName(), type);
        table.addTypeToScope(entry);
        table.createTypeScope();
        visitExp(dec.getConvention());
        if (dec.getInitialization() != null) {
            visitInitItem(dec.getInitialization());
        }
        if (dec.getFinalization() != null) {
            visitFinalItem(dec.getFinalization());
        }
        table.completeTypeScope();
    }

    public void visitTypeDec(TypeDec dec) {
        Type type = getConceptualType(dec.getModel(), dec.getName());
        TypeEntry entry = new TypeEntry(table.getCurrentScope(), dec.getName(), type, dec.getExemplar());
        table.addTypeToScope(entry);
        table.createTypeScope();
        VarEntry ex = new VarEntry(table.getCurrentScope(), Mode.EXEMPLAR, dec.getExemplar(), type);
        table.addVariableToScope(ex);
        visitExp(dec.getConstraint());
        if (dec.getInitialization() != null) {
            visitInitItem(dec.getInitialization());
        }
        if (dec.getFinalization() != null) {
            visitFinalItem(dec.getFinalization());
        }
        table.completeTypeScope();
    }

    // changed this to handle local type declarations
    public void visitRepresentationDec(RepresentationDec dec) {
        Type type = getProgramType(dec.getRepresentation(), dec.getName());
        TypeEntry entry = new TypeEntry(table.getCurrentScope(), dec.getName(), type);
        table.addTypeToScope(entry);
        table.createTypeScope();
        // re-do;
        // essentially copied and added the following code from
        // addTypeFamilyVariablesToScope
        boolean isLocalType = true;
        TypeEntry specentry = null;
        Symbol tsym = entry.getName().getSymbol();
        Iterator<ModuleID> i = table.getModuleScope().getSpecIterator();
        while (i.hasNext()) {
            ModuleScope scope = myInstanceEnvironment.getModuleScope(i.next());
            if (scope.containsLocalConcType(tsym)) {
                specentry = scope.getLocalType(tsym);
                isLocalType = false;
            }
        }
        if (!isLocalType) {
            addTypeFamilyVariablesToScope(entry);
            visitExp(dec.getConvention());
            visitExp(dec.getCorrespondence());
            if (dec.getInitialization() != null) {
                visitInitItem(dec.getInitialization());
            }
            if (dec.getFinalization() != null) {
                visitFinalItem(dec.getFinalization());
            }
        }
        table.completeTypeScope();
    }

    // -----------------------------------------------------------
    // Operation Declarations
    // -----------------------------------------------------------

    public void visitFacilityOperationDec(FacilityOperationDec dec) {
        List<VarEntry> vars = getVariables(dec.getParameters());
        OperationEntry oper =
                new OperationEntry(table.getCurrentScope(), dec.getName(), vars, getProgramType(dec
                        .getReturnTy()));
        table.addOperationToScope(oper);
        table.createOperationScope(dec.getName());
        addVariablesToScope(vars);
        visitExp(dec.getRequires());
        visitExp(dec.getEnsures());
        table.createProcedureScope(dec.getName());
        addVariablesToScope(vars);
        if (dec.getReturnTy() != null) {
            VarEntry var =
                    new VarEntry(table.getCurrentScope(), Mode.OPER_NAME, dec.getName(), getProgramType(dec
                            .getReturnTy()));
            table.addVariableToScope(var);
        }
        visitExp(dec.getDecreasing());
        visitFacilityDecList(dec.getFacilities());
        visitLocalVariables(dec.getVariables());
        visitStatementList(dec.getStatements());
        table.completeProcedureScope();
        table.completeOperationScope();
    }

    public void visitOperationDec(OperationDec dec) {
        List<VarEntry> vars = getVariables(dec.getParameters());
        OperationEntry oper =
                new OperationEntry(table.getCurrentScope(), dec.getName(), vars, getProgramType(dec
                        .getReturnTy()));
        table.addOperationToScope(oper);
        table.createOperationScope(dec.getName());
        addVariablesToScope(vars);
        visitExp(dec.getRequires());
        visitExp(dec.getEnsures());
        table.completeOperationScope();
    }

    public void visitProofDec(ProofDec dec) {
        Symbol s = Symbol.symbol("PROOF_" + dec.getName().getName());
        PosSymbol ps = new PosSymbol(dec.getName().getLocation(), s);
        ProofEntry pentry = new ProofEntry(ps);
        table.addProofToScope(pentry);
        table.createProofScope(ps);
        if (dec.getStatements().size() != 0) {
            Iterator<Exp> it = dec.getStatements().iterator();
            while (it.hasNext()) {
                visitExp(it.next());
            }
        }
        if (dec.getBaseCase().size() != 0) {
            Iterator<Exp> it = dec.getBaseCase().iterator();
            while (it.hasNext()) {
                visitExp(it.next());
            }
        }
        if (dec.getInductiveCase().size() != 0) {
            Iterator<Exp> it = dec.getInductiveCase().iterator();
            while (it.hasNext()) {
                visitExp(it.next());
            }
        }
        table.completeProofScope();
    }

    public void visitProcedureDec(ProcedureDec dec) {
        List<VarEntry> vars = getVariables(dec.getParameters());
        OperationEntry oper =
                new OperationEntry(table.getCurrentScope(), dec.getName(), vars, getProgramType(dec
                        .getReturnTy()));
        table.addOperationToScope(oper);
        table.createOperationScope(dec.getName());
        addVariablesToScope(vars);
        table.createProcedureScope(dec.getName());
        addVariablesToScope(vars);

        if (dec.getReturnTy() != null) {
            VarEntry var =
                    new VarEntry(table.getCurrentScope(), Mode.OPER_NAME, dec.getName(), getProgramType(dec
                            .getReturnTy()));
            table.addVariableToScope(var);
        }
        visitFacilityDecList(dec.getFacilities());
        visitLocalVariables(dec.getAllVariables());
        visitStatementList(dec.getStatements());
        table.completeProcedureScope();
        table.completeOperationScope();
    }

    // -----------------------------------------------------------
    // Facility Declarations
    // -----------------------------------------------------------

    public void visitFacilityDec(FacilityDec dec) {
        int initErrorCount = err.getErrorCount();
        List<ModuleScope> scopes = getConceptualModules(dec);
        List<ModuleScope> iscopes = new List<ModuleScope>();
        ModuleScope newscope = new ModuleScope(dec, myInstanceEnvironment);
        Binding newbind = newscope.getBinding();
        Map<Symbol, Type> typeMap = getTypeMappings(dec);
        if (err.countExceeds(initErrorCount)) {
            // The number of arguments is not the same as number of
            // parameters in one or more module argument lists.
            return;
        }
        Iterator<ModuleScope> i = scopes.iterator();
        while (i.hasNext()) {
            ModuleScope scope = i.next();
            ModuleScope iscope = scope.instantiate(dec, typeMap, newbind);
            iscopes.add(iscope);
        }
        try {
            newscope.merge(iscopes);
            ModuleEntry module = new ModuleEntry(dec.getName(), newscope);
            table.addFacilityToScope(module);
        }
        catch (InstantiationException ex) {
            err.error(dec.getName().getLocation(), ex.getMessage());
        }
    }

    // -----------------------------------------------------------
    // Module Parameter Declarations
    // -----------------------------------------------------------

    public void visitConceptTypeParamDec(ConceptTypeParamDec dec) {
        Type type = new FormalType(table.getModuleID(), dec.getName());
        TypeEntry entry = new TypeEntry(table.getCurrentScope(), dec.getName(), type);
        table.addTypeToScope(entry);
        table.addModuleParameter(entry);
    }

    public void visitConstantParamDec(ConstantParamDec dec) {
        VarEntry var =
                new VarEntry(table.getCurrentScope(), Mode.EVALUATES, dec.getName(), getProgramType(dec
                        .getTy()));
        table.addVariableToScope(var);
        table.addModuleParameter(var);
    }

    public void visitRealizationParamDec(RealizationParamDec dec) {
    // FIX: Postpone this till later, since it doesn't even
    // work like we want it to.
    }

    // ===========================================================
    // Public Methods - Non-declarative Constructs
    // ===========================================================

    public void visitAffectsItem(AffectsItem item) {
        ;
    }

    public void visitChoiceItem(ChoiceItem item) {
        visitStatementList(item.getThenclause());
    }

    public void visitConditionItem(ConditionItem item) {
        visitStatementList(item.getThenclause());
    }

    public void visitEnhancementBodyItem(EnhancementBodyItem item) {
    //
    }

    public void visitEnhancementItem(EnhancementItem item) {
    //
    }

    public void visitFinalItem(FinalItem item) {
        PosSymbol name = createFinalName(item.getLocation());
        table.createOperationScope(name); // for eventual adding of defs
        visitExp(item.getRequires());
        visitExp(item.getEnsures());
        table.createProcedureScope(name);
        visitFacilityDecList(item.getFacilities());
        visitLocalVariables(item.getVariables());
        visitStatementList(item.getStatements());
        table.completeProcedureScope();
        table.completeOperationScope();
    }

    public void visitFunctionArgList(FunctionArgList list) {
        visitExpList(list.getArguments());
    }

    public void visitInitItem(InitItem item) {
        PosSymbol name = createInitName(item.getLocation());
        table.createOperationScope(name); // for eventual adding of defs
        visitExp(item.getRequires());
        visitExp(item.getEnsures());
        table.createProcedureScope(name);
        visitFacilityDecList(item.getFacilities());
        visitLocalVariables(item.getVariables());
        visitStatementList(item.getStatements());
        table.completeProcedureScope();
        table.completeOperationScope();
    }

    public void visitModuleArgumentItem(ModuleArgumentItem item) {
    //
    }

    public void visitRenamingItem(RenamingItem item) {
    //
    }

    public void visitUsesItem(UsesItem item) {
        ModuleID fid = ModuleID.createFacilityID(item.getName());
        if (myInstanceEnvironment.contains(fid)) {
            // A simple check to ensure that the Standard Facilities are not
            // added a second time because they are implicitly included in the
            // VisitModuleDec()
            // Similar check found in ImportScanner.visitUsesItem()
            String[] stdUses = myInstanceEnvironment.getStdUses();
            String itemName = item.getName().getName();
            boolean doAnnex = true;
            for (int i = 0; i < stdUses.length; i++) {
                if (itemName.equals("Std_" + stdUses[i] + "_Fac"))
                    doAnnex = false;
            }
            if (doAnnex) {
                ModuleEntry entry =
                        new ModuleEntry(item.getName(), myInstanceEnvironment.getSymbolTable(fid)
                                .getModuleScope());
                table.addFacilityToScope(entry);
            }
        }
    }

    // ===========================================================
    // Statements
    // ===========================================================

    public void visitCallStmt(CallStmt stmt) {
        ;
    }

    public void visitFuncAssignStmt(FuncAssignStmt stmt) {
        ;
    }

    public void visitIfStmt(IfStmt stmt) {
        visitStatementList(stmt.getThenclause());
        visitConditionItemList(stmt.getElseifpairs());
        visitStatementList(stmt.getElseclause());
    }

    public void visitIterateStmt(IterateStmt stmt) {
        table.createStatementScope();
        visitExp(stmt.getMaintaining());
        visitExp(stmt.getDecreasing());
        visitStatementList(stmt.getStatements());
        table.completeStatementScope();
    }

    public void visitIterateExitStmt(IterateExitStmt stmt) {
        visitStatementList(stmt.getStatements());
    }

    public void visitMemoryStmt(MemoryStmt stmt) {
        ;
    }

    public void visitSelectionStmt(SelectionStmt stmt) {
        visitChoiceItemList(stmt.getWhenpairs());
        visitStatementList(stmt.getDefaultclause());
    }

    public void visitSwapStmt(SwapStmt stmt) {
        ;
    }

    public void visitWhileStmt(WhileStmt stmt) {
        table.createStatementScope();
        visitExp(stmt.getMaintaining());
        visitExp(stmt.getDecreasing());
        visitStatementList(stmt.getStatements());
        table.completeStatementScope();
    }

    // ===========================================================
    // Public Methods - Expressions
    // ===========================================================

    // -----------------------------------------------------------
    // Literal Expressions
    // -----------------------------------------------------------

    public void visitCharExp(CharExp exp) {
        ;
    }

    public void visitDoubleExp(DoubleExp exp) {
        ;
    }

    public void visitIntegerExp(IntegerExp exp) {
        ;
    }

    public void visitStringExp(StringExp exp) {
        ;
    }

    // -----------------------------------------------------------
    // Operational Expressions
    // -----------------------------------------------------------

    public void visitAlternativeExp(AlternativeExp exp) {
        Iterator<AltItemExp> i = exp.getAlternatives().iterator();
        while (i.hasNext()) {
            visitExp(i.next());
        }
    }

    public void visitAltItemExp(AltItemExp exp) {
        visitExp(exp.getTest());
        visitExp(exp.getAssignment());
    }

    public void visitBetweenExp(BetweenExp exp) {
        Iterator<Exp> i = exp.getLessExps().iterator();
        while (i.hasNext()) {
            visitExp(i.next());
        }
    }

    public void visitEqualsExp(EqualsExp exp) {
        visitExp(exp.getLeft());
        visitExp(exp.getRight());
    }

    public void visitIfExp(IfExp exp) {
        visitExp(exp.getTest());
        visitExp(exp.getThenclause());
        visitExp(exp.getElseclause());
    }

    public void visitInfixExp(InfixExp exp) {
        visitExp(exp.getLeft());
        visitExp(exp.getRight());
    }

    public void visitIterativeExp(IterativeExp exp) {
        table.createExpressionScope();
        MathVarDec var = exp.getVar();
        VarEntry ventry =
                new VarEntry(table.getCurrentScope(), Mode.MATH, var.getName(), getMathType(var.getTy()));
        table.addVariableToScope(ventry);
        visitExp(exp.getWhere());
        visitExp(exp.getBody());
        table.endExpressionScope();
    }

    public void visitLambdaExp(LambdaExp exp) {
        table.createExpressionScope();

        for (MathVarDec p : exp.getParameters()) {
            VarEntry ve =
                    new VarEntry(table.getCurrentScope(), Mode.MATH, p.getName(), getMathType(p.getTy()));
            table.addVariableToScope(ve);
        }

        table.completeExpressionScope();
    }

    public void visitOutfixExp(OutfixExp exp) {
        visitExp(exp.getArgument());
    }

    public void visitPrefixExp(PrefixExp exp) {
        visitExp(exp.getArgument());
    }

    public void visitQuantExp(QuantExp exp) {
        table.createExpressionScope();
        visitQuantifiedVariables(exp.getVars());
        visitExp(exp.getWhere());
        visitExp(exp.getBody());
        table.completeExpressionScope();
    }

    public void visitSetExp(SetExp exp) {
        table.createExpressionScope();
        List<MathVarDec> list = new List<MathVarDec>();
        list.add(exp.getVar());
        visitQuantifiedVariables(list);
        visitExp(exp.getWhere());
        visitExp(exp.getBody());
        table.completeExpressionScope();
    }

    public void visitTupleExp(TupleExp exp) {
        Iterator<Exp> i = exp.getFields().iterator();
        while (i.hasNext()) {
            visitExp(i.next());
        }
    }

    public void visitUnaryMinusExp(UnaryMinusExp exp) {
        visitExp(exp.getArgument());
    }

    // -----------------------------------------------------------
    // Dot Expressions
    // -----------------------------------------------------------

    public void visitDotExp(DotExp exp) {
        Iterator<Exp> i = exp.getSegments().iterator();
        while (i.hasNext()) {
            visitExp(i.next());
        }
    }

    public void visitFieldExp(FieldExp exp) {
        assert false : "This expression is only constructed during analysis";
    }

    public void visitOldExp(OldExp exp) {
        visitExp(exp.getExp());
    }

    public void visitFunctionExp(FunctionExp exp) {
        visitExp(exp.getNatural());
        Iterator<FunctionArgList> i = exp.getParamList().iterator();
        while (i.hasNext()) {
            visitFunctionArgList(i.next());
        }
    }

    public void visitVarExp(VarExp exp) {
        ;
    }

    public void visitTypeFunctionExp(TypeFunctionExp exp) {
        Iterator<Exp> i = exp.getParams().iterator();
        while (i.hasNext()) {
            visitExp(i.next());
        }
    }

    // -----------------------------------------------------------
    // Program Expressions
    // -----------------------------------------------------------

    /*
     * Because program expressions can never contain declarations (as opposed to
     * math expressions), we do not need to visit them to populate the symbol
     * table.
     */

    // ===========================================================
    // Private Methods
    // ===========================================================

    private void addTypeFamilyVariablesToScope(TypeEntry entry) {
        TypeEntry specentry = null;
        Symbol tsym = entry.getName().getSymbol();
        Iterator<ModuleID> i = table.getModuleScope().getSpecIterator();
        while (i.hasNext()) {
            ModuleScope scope = myInstanceEnvironment.getModuleScope(i.next());
            if (scope.containsLocalConcType(tsym)) {
                specentry = scope.getLocalType(tsym);
            }
        }
        if (entry == null) {
            String msg = cantFindFamilyMessage(tsym.toString());
            err.error(entry.getName().getLocation(), msg);
            return;
        }
        else {
            VarEntry progexemp =
                    new VarEntry(table.getCurrentScope(), Mode.EXEMPLAR, specentry.getExemplar(), entry
                            .getType());
            table.addVariableToScope(progexemp);
            VarEntry concexemp =
                    new VarEntry(table.getCurrentScope(), Mode.EXEMPLAR, createConcName(specentry
                            .getExemplar()), specentry.getType().toMath());
            table.addVariableToScope(concexemp);
        }
    }

    private PosSymbol createConcName(PosSymbol name) {
        return new PosSymbol(name.getLocation(), Symbol.symbol("%Conc." + name.getSymbol().toString()));
    }

    // -----------------------------------------------------------
    // Iterative Visit Methods
    // -----------------------------------------------------------

    private void visitChoiceItemList(List<ChoiceItem> items) {
        Iterator<ChoiceItem> i = items.iterator();
        while (i.hasNext()) {
            visitChoiceItem(i.next());
        }
    }

    private void visitConditionItemList(List<ConditionItem> items) {
        Iterator<ConditionItem> i = items.iterator();
        while (i.hasNext()) {
            visitConditionItem(i.next());
        }
    }

    private void visitExpList(List<Exp> exps) {
        Iterator<Exp> i = exps.iterator();
        while (i.hasNext()) {
            visitExp(i.next());
        }
    }

    private void visitFacilityDecList(List<FacilityDec> decs) {
        Iterator<FacilityDec> i = decs.iterator();
        while (i.hasNext()) {
            visitFacilityDec(i.next());
        }
    }

    private void visitStatementList(List<Statement> stmts) {
        if (stmts != null) {
            Iterator<Statement> i = stmts.iterator();
            while (i.hasNext()) {
                visitStatement(i.next());
            }
        }
    }

    private void visitUsesItemList(List<UsesItem> items) {
        if (items == null)
            return;
        Iterator<UsesItem> i = items.iterator();
        while (i.hasNext()) {
            visitUsesItem(i.next());
        }
    }

    // -----------------------------------------------------------
    // Module Level Declaration Processing
    // -----------------------------------------------------------

    private void visitDecList(List<Dec> decs) {
        Iterator<Dec> i = decs.iterator();
        while (i.hasNext()) {
            Dec dec = i.next();
            if (dec instanceof MathVarDec) {
                visitConceptualVariable((MathVarDec) dec);
            }
            else if (dec instanceof VarDec) {
                visitStateVariable((VarDec) dec);
            }
            else {
                visitDec(dec);
            }
        }
    }

    private void visitConceptualVariable(MathVarDec dec) {
        if (dec.getConfirm() == true) {
            TypeConverter tc = new TypeConverter(table);
            Type t = tc.getMathType(dec.getTy());
            table.addAlternateVarType(dec);
        }
        else {
            VarEntry var =
                    new VarEntry(table.getCurrentScope(), Mode.CONCEPTUAL, dec.getName(), getMathType(dec
                            .getTy()));
            table.addVariableToScope(var);
        }
    }

    private void visitStateVariable(VarDec dec) {
        VarEntry var =
                new VarEntry(table.getCurrentScope(), Mode.STATE, dec.getName(), getProgramType(dec.getTy()));
        table.addVariableToScope(var);
    }

    // -----------------------------------------------------------
    // Module Param Processing
    // -----------------------------------------------------------

    private void visitModuleParameterList(List<ModuleParameterDec> pars) {
        if (pars == null) {
            return;
        }
        Iterator<ModuleParameterDec> i = pars.iterator();
        while (i.hasNext()) {
            Dec dec = castToDec(i.next());
            if (dec instanceof DefinitionDec) {
                visitModParamDefDec((DefinitionDec) dec);
            }
            else if (dec instanceof OperationDec) {
                visitModParamOperDec((OperationDec) dec);
            }
            else {
                visitDec(dec);
            }
        }
    }

    private void visitModParamDefDec(DefinitionDec dec) {
        VarEntry var = null;
        if (dec.getParameters() == null) {
            var =
                    new VarEntry(table.getCurrentScope(), Mode.DEFINITION, dec.getName(), getMathType(dec
                            .getReturnTy()));
        }
        else {
            var = new VarEntry(table.getCurrentScope(), Mode.DEFINITION, dec.getName(), getFunctionType(dec));
        }
        table.addVariableToScope(var);
        // FIX: We should not need to add scopes here, we should avoid
        // beginning and ending scopes in analysis.
        table.createDefinitionScope(dec.getName());
        table.completeDefinitionScope();
        table.addModuleParameter(var);
    }

    private void visitModParamOperDec(OperationDec dec) {
        List<VarEntry> vars = getVariables(dec.getParameters());
        OperationEntry oper =
                new OperationEntry(table.getCurrentScope(), dec.getName(), vars, getProgramType(dec
                        .getReturnTy()));
        table.addOperationToScope(oper);
        table.addModuleParameter(oper);
        table.createOperationScope(dec.getName());
        addVariablesToScope(vars);
        visitExp(dec.getRequires());
        visitExp(dec.getEnsures());
        table.completeOperationScope();
    }

    // -----------------------------------------------------------
    // Other Expression Types
    // -----------------------------------------------------------

    public void visitGoalExp(GoalExp exp) {
        visitExp(exp.getExp());
    }

    public void visitSuppositionExp(SuppositionExp exp) {
        visitExp(exp.getExp());
    }

    public void visitDeductionExp(DeductionExp exp) {
        visitExp(exp.getExp());
    }

    public void visitJustifiedExp(JustifiedExp exp) {
        visitExp(exp.getExp());
    }

    public void visitProofDefinitionExp(ProofDefinitionExp exp) {
        visitDefinitionDec((DefinitionDec) (exp.getExp()));
    }

    public void visitSuppositionDeductionExp(SuppositionDeductionExp exp) {
        table.createExpressionScope();
        if (exp.getSupposition().getVars() != null) {
            visitQuantifiedVariables(exp.getSupposition().getVars());
        }
        if (exp.getSupposition().getExp() != null) {
            visitExp(exp.getSupposition());
        }
        visitSupDeducBody(exp);
        table.completeExpressionScope();
    }

    private void visitSupDeducBody(SuppositionDeductionExp exp) {
        Iterator<Exp> i = exp.getBody().iterator();
        while (i.hasNext()) {
            visitExp(i.next());
        }
        visitExp(exp.getDeduction());
    }

    // -----------------------------------------------------------
    // Type Translation
    // -----------------------------------------------------------

    private List<Type> getProgramTypes(List<Ty> tys) {
        List<Type> retval = new List<Type>();
        for (Ty t : tys) {
            retval.add(getProgramType(t));
        }
        return retval;
    }

    private Type getProgramType(Ty ty) {
        TypeConverter tc = new TypeConverter(table);
        return tc.getProgramType(ty);
    }

    private Type getMathType(Ty ty) {
        TypeConverter tc = new TypeConverter(table);
        return tc.getMathType(ty);
    }

    // private Type getProgramType(Ty ty) {
    // if (ty == null) {
    // return new VoidType();
    // } else if (ty instanceof ArrayTy) {
    // return getArrayType((ArrayTy)ty);
    // } else if (ty instanceof NameTy) {
    // return getProgramIndirectType((NameTy)ty);
    // } else if (ty instanceof RecordTy) {
    // return getRecordType((RecordTy)ty);
    // } else {
    // assert false : "ty is invalid";
    // return null;
    // }
    // }
    //
    // public Type getMathType(Ty ty) {
    // if (ty instanceof CartProdTy) {
    // return getTupleType((CartProdTy)ty);
    // } else if (ty instanceof ConstructedTy) {
    // return getConstructedType((ConstructedTy)ty);
    // } else if (ty instanceof FunctionTy) {
    // return getFunctionType((FunctionTy)ty);
    // } else if (ty instanceof NameTy) {
    // return getMathIndirectType((NameTy)ty);
    // } else if (ty instanceof TupleTy) {
    // return getTupleType((TupleTy)ty);
    // } else {
    // assert false : "ty is invalid";
    // return null;
    // }
    // }
    //
    // private Type getArrayType(ArrayTy ty) {
    // PosSymbol intqual = new PosSymbol(ty.getLocation(),
    // Symbol.symbol("Std_Integer_Fac"));
    // PosSymbol intname = new PosSymbol(ty.getLocation(),
    // Symbol.symbol("Integer"));
    // NameTy index = new NameTy(intqual, intname);
    // return new ArrayType(table.getModuleID(),
    // createArrayName(ty.getLocation()),
    // ty.getLo(), ty.getHi(), getProgramType(index),
    // getProgramType(ty.getEntryType()));
    // }
    //
    // private Type getProgramIndirectType(NameTy ty) {
    // Binding binding = table.getCurrentBinding();
    // binding.addProgramIndirectName(ty.getQualifier(), ty.getName());
    // return new IndirectType(ty.getQualifier(), ty.getName(), binding);
    // }
    //
    // private Type getRecordType(RecordTy ty) {
    // List<FieldItem> fields = new List<FieldItem>();
    // Iterator<VarDec> i = ty.getFields().iterator();
    // while (i.hasNext()) {
    // VarDec var = i.next();
    // FieldItem item = new FieldItem(var.getName(),
    // getProgramType(var.getTy()));
    // fields.add(item);
    // }
    // return new RecordType(table.getModuleID(),
    // createRecordName(ty.getLocation()), fields);
    // }
    //
    // private Type getTupleType(CartProdTy ty) {
    // List<FieldItem> fields = new List<FieldItem>();
    // Iterator<MathVarDec> i = ty.getFields().iterator();
    // while (i.hasNext()) {
    // MathVarDec var = i.next();
    // FieldItem item = new FieldItem(var.getName(),
    // getMathType(var.getTy()));
    // fields.add(item);
    // }
    // return new TupleType(fields);
    // }
    //
    // private Type getConstructedType(ConstructedTy ty) {
    // Binding binding = table.getCurrentBinding();
    // binding.addConstructedName(ty.getQualifier(), ty.getName(),
    // ty.getArgs().size());
    // List<Type> args = new List<Type>();
    // Iterator<Ty> i = ty.getArgs().iterator();
    // while (i.hasNext()) {
    // Ty ty2 = i.next();
    // Type type = getMathType(ty2);
    // args.add(type);
    // }
    // return new ConstructedType(ty.getQualifier(), ty.getName(), args,
    // table.getCurrentBinding());
    // }
    //

    private Type getFunctionType(FunctionTy ty) {
        TypeConverter tc = new TypeConverter(table);
        return new FunctionType(tc.getMathType(ty.getDomain()), tc.getMathType(ty.getRange()));
    }

    // private Type getFunctionType(FunctionTy ty) {
    // return new FunctionType(getMathType(ty.getDomain()),
    // getMathType(ty.getRange()));
    // }
    //
    // private Type getMathIndirectType(NameTy ty) {
    // Binding binding = table.getCurrentBinding();
    // binding.addMathIndirectName(ty.getQualifier(), ty.getName());
    // return new IndirectType(ty.getQualifier(), ty.getName(), binding);
    // }
    //
    // private Type getTupleType(TupleTy ty) {
    // List<FieldItem> fields = new List<FieldItem>();
    // Iterator<Ty> i = ty.getFields().iterator();
    // while (i.hasNext()) {
    // Ty ty2 = i.next();
    // PosSymbol name = new PosSymbol(ty.getLocation(),
    // Symbol.symbol(""));
    // FieldItem item = new FieldItem(name, getMathType(ty2));
    // fields.add(item);
    // }
    // return new TupleType(fields);
    // }
    //

    private Type getProgramType(Ty ty, PosSymbol name) {
        TypeConverter tc = new TypeConverter(table);
        return tc.getProgramType(ty, name);
    }

    // private Type getProgramType(Ty ty, PosSymbol name) {
    // Type type = getProgramType(ty);
    // if (type instanceof ArrayType) {
    // ((ArrayType)type).setName(name);
    // }
    // if (type instanceof RecordType) {
    // ((RecordType)type).setName(name);
    // }
    // // if (type instanceof IndirectType) {
    // // type = new NameType(table.getModuleID(), name, type);
    // // }
    // return type;
    // }
    //
    private Type getConceptualType(Ty ty, PosSymbol name) {
        TypeConverter tc = new TypeConverter(table);
        return tc.getConceptualType(ty, name);
    }

    // private Type getConceptualType(Ty ty, PosSymbol name) {
    // return new ConcType(table.getModuleID(), name, getMathType(ty));
    // }
    //
    // // -----------------------------------------------------------
    // // Type Translation for Definitions
    // // -----------------------------------------------------------
    //

    private Type getFunctionType(DefinitionDec dec) {
        assert dec.getParameters().size() > 0 : "function takes no parameters";
        TypeConverter tc = new TypeConverter(table);
        return new FunctionType(tc.getTupleType(dec.getParameters()), tc.getMathType(dec.getReturnTy()));
    }

    // private Type getFunctionType(DefinitionDec dec) {
    // assert dec.getParameters().size() > 0 :
    // "function takes not parameters";
    // return new FunctionType(getTupleType(dec.getParameters()),
    // getMathType(dec.getReturnTy()));
    // }
    //
    // private Type getTupleType(List<MathVarDec> decs) {
    // List<FieldItem> fields = new List<FieldItem>();
    // Iterator<MathVarDec> i = decs.iterator();
    // while (i.hasNext()) {
    // MathVarDec dec = i.next();
    // FieldItem item = new FieldItem(dec.getName(),
    // getMathType(dec.getTy()));
    // fields.add(item);
    // }
    // return new TupleType(fields);
    // }

    // -----------------------------------------------------------
    // Visit Variables (and add to scope)
    // -----------------------------------------------------------

    private void visitDefinitionParameters(List<MathVarDec> decs) {
        if (decs != null) {
            Iterator<MathVarDec> i = decs.iterator();
            while (i.hasNext()) {
                MathVarDec dec = i.next();
                VarEntry var =
                        new VarEntry(table.getCurrentScope(), Mode.DEF_PARAM, dec.getName(), getMathType(dec
                                .getTy()));
                table.addVariableToScope(var);

                if (dec.getTy() instanceof NameTy) {
                    NameTy decAsNameTy = (NameTy) dec.getTy();
                    if (decAsNameTy.getName().getName().equals("SSet")) {
                        Type type = new FormalType(null, dec.getName());
                        TypeEntry entry = new TypeEntry(table.getCurrentScope(), dec.getName(), type);
                        table.addTypeToScope(entry);
                    }
                }
                /*
                 * Type type = new FormalType(table.getModuleID(),
                 * dec.getName()); TypeEntry entry = new
                 * TypeEntry(table.getCurrentScope(), dec.getName(), type);
                 * table.addTypeToScope(entry);
                 */
            }
        }
    }

    private void visitQuantifiedVariables(List<MathVarDec> decs) {
        Iterator<MathVarDec> i = decs.iterator();
        while (i.hasNext()) {
            MathVarDec dec = i.next();
            VarEntry var =
                    new VarEntry(table.getCurrentScope(), Mode.MATH, dec.getName(), getMathType(dec.getTy()));
            table.addVariableToScope(var);
        }
    }

    private void visitLocalVariables(List<VarDec> decs) {
        Iterator<VarDec> i = decs.iterator();
        while (i.hasNext()) {
            VarDec dec = i.next();
            VarEntry var =
                    new VarEntry(table.getCurrentScope(), Mode.LOCAL, dec.getName(), getProgramType(dec
                            .getTy()));
            table.addVariableToScope(var);
        }
    }

    // -----------------------------------------------------------
    // Getting Variables (without adding to scope)
    // -----------------------------------------------------------

    private List<VarEntry> getVariables(List<ParameterVarDec> decs) {
        List<VarEntry> vars = new List<VarEntry>();
        Iterator<ParameterVarDec> i = decs.iterator();
        while (i.hasNext()) {
            ParameterVarDec dec = i.next();
            VarEntry var =
                    new VarEntry(table.getCurrentScope(), dec.getMode(), dec.getName(), getProgramType(dec
                            .getTy()));
            vars.add(var);
        }
        return vars;
    }

    // -----------------------------------------------------------
    // Add Var Entry List to Scope
    // -----------------------------------------------------------

    private void addVariablesToScope(List<VarEntry> vars) {
        Iterator<VarEntry> i = vars.iterator();
        while (i.hasNext()) {
            table.addVariableToScope(i.next());
        }
    }

    // -----------------------------------------------------------
    // Symbol Table Add Methods
    // -----------------------------------------------------------

    private void addAssocEnhancementsToTable(List<EnhancementBodyItem> items, PosSymbol cName) {
        Iterator<EnhancementBodyItem> i = items.iterator();
        while (i.hasNext()) {
            table.addAssocEnhancement(i.next().getName(), cName);
        }
    }

    // -----------------------------------------------------------
    // Facility Instantiation Methods
    // -----------------------------------------------------------

    private List<ModuleScope> getConceptualModules(FacilityDec dec) {
        List<ModuleScope> scopes = new List<ModuleScope>();
        ModuleID cid = ModuleID.createConceptID(dec.getConceptName());
        scopes.add(myInstanceEnvironment.getSymbolTable(cid).getModuleScope());
        scopes.addAll(getEnhConceptualModules(dec.getEnhancements(), dec.getConceptName()));
        scopes.addAll(getEnhBodConceptualModules(dec.getEnhancementBodies(), dec.getConceptName()));
        return scopes;
    }

    private List<ModuleScope> getEnhConceptualModules(List<EnhancementItem> items, PosSymbol cname) {
        List<ModuleScope> scopes = new List<ModuleScope>();
        Iterator<EnhancementItem> i = items.iterator();
        while (i.hasNext()) {
            EnhancementItem item = i.next();
            ModuleID eid = ModuleID.createEnhancementID(item.getName(), cname);
            scopes.add(myInstanceEnvironment.getSymbolTable(eid).getModuleScope());
        }
        return scopes;
    }

    private List<ModuleScope> getEnhBodConceptualModules(List<EnhancementBodyItem> items, PosSymbol cname) {
        List<ModuleScope> scopes = new List<ModuleScope>();
        Iterator<EnhancementBodyItem> i = items.iterator();
        while (i.hasNext()) {
            EnhancementBodyItem item = i.next();
            ModuleID eid = ModuleID.createEnhancementID(item.getName(), cname);
            scopes.add(myInstanceEnvironment.getSymbolTable(eid).getModuleScope());
        }
        return scopes;
    }

    private List<Binding> getBindings(List<ModuleScope> scopes) {
        List<Binding> bindings = new List<Binding>();
        Iterator<ModuleScope> i = scopes.iterator();
        while (i.hasNext()) {
            ModuleScope scope = i.next();
            bindings.add(scope.getBinding());
        }
        return bindings;
    }

    private Map<Symbol, Type> getTypeMappings(FacilityDec dec) {
        Map<Symbol, Type> typeMap = new Map<Symbol, Type>();
        ModuleID cid = ModuleID.createConceptID(dec.getConceptName());
        ModuleScope scope = myInstanceEnvironment.getSymbolTable(cid).getModuleScope();
        List<Entry> pars = scope.getModuleParameters();
        if (dec.getConceptParams().size() < pars.size()) {
            String msg = moreParsThanArgsMessage();
            err.error(dec.getConceptName().getLocation(), msg);
            return typeMap;
        }
        if (dec.getConceptParams().size() > pars.size()) {
            String msg = moreArgsThanParsMessage();
            err.error(dec.getConceptName().getLocation(), msg);
            return typeMap;
        }
        typeMap.putAll(getParamTypeMappings(pars, dec.getConceptParams()));
        typeMap.putAll(getEnhTypeMappings(dec.getEnhancements(), dec.getConceptName()));
        typeMap.putAll(getEnhBodTypeMappings(dec.getEnhancementBodies(), dec.getConceptName()));
        return typeMap;
    }

    private Map<Symbol, Type> getEnhTypeMappings(List<EnhancementItem> items, PosSymbol cname) {
        Map<Symbol, Type> typeMap = new Map<Symbol, Type>();
        Iterator<EnhancementItem> i = items.iterator();
        while (i.hasNext()) {
            EnhancementItem item = i.next();
            ModuleID eid = ModuleID.createEnhancementID(item.getName(), cname);
            ModuleScope scope = myInstanceEnvironment.getSymbolTable(eid).getModuleScope();
            List<Entry> pars = scope.getModuleParameters();
            if (item.getParams().size() < pars.size()) {
                String msg = moreParsThanArgsMessage();
                err.error(item.getName().getLocation(), msg);
                break;
            }
            if (item.getParams().size() > pars.size()) {
                String msg = moreArgsThanParsMessage();
                err.error(item.getName().getLocation(), msg);
                break;
            }
            typeMap.putAll(getParamTypeMappings(pars, item.getParams()));
        }
        return typeMap;
    }

    private Map<Symbol, Type> getEnhBodTypeMappings(List<EnhancementBodyItem> items, PosSymbol cname) {
        Map<Symbol, Type> typeMap = new Map<Symbol, Type>();
        Iterator<EnhancementBodyItem> i = items.iterator();
        while (i.hasNext()) {
            EnhancementBodyItem item = i.next();
            ModuleID eid = ModuleID.createEnhancementID(item.getName(), cname);
            ModuleScope scope = myInstanceEnvironment.getSymbolTable(eid).getModuleScope();
            List<Entry> pars = scope.getModuleParameters();
            if (item.getParams().size() < pars.size()) {
                String msg = moreParsThanArgsMessage();
                err.error(item.getName().getLocation(), msg);
                break;
            }
            if (item.getParams().size() > pars.size()) {
                String msg = moreArgsThanParsMessage();
                err.error(item.getName().getLocation(), msg);
                break;
            }
            typeMap.putAll(getParamTypeMappings(pars, item.getParams()));
        }
        return typeMap;
    }

    private Map<Symbol, Type> getParamTypeMappings(List<Entry> parameters, List<ModuleArgumentItem> arguments) {

        Map<Symbol, Type> typeMap = new Map<Symbol, Type>();

        Iterator<Entry> iterParameters = parameters.iterator();
        Iterator<ModuleArgumentItem> iterArguments = arguments.iterator();

        while (iterParameters.hasNext()) {
            Entry curParameter = iterParameters.next();
            ModuleArgumentItem curArgument = iterArguments.next();

            if (curParameter instanceof TypeEntry) {
                Symbol sym = curParameter.getSymbol();
                if (curArgument.getName() == null) {
                    String msg = expForTypeMessage();
                    err.error(curArgument.getEvalExp().getLocation(), msg);
                    break;
                } /*
                   * else { // code to make sure that the actual is a type Binding
                   * bind = table.getCurrentBinding(); if
                   * (!bind.contains(item.getName())) { String msg =
                   * expForTypeMessage(); err.error(item.getName().getLocation(),
                   * msg); break; } else { String typeName =
                   * item.getName().toString(); Binding binding =
                   * table.getModuleScope().getBinding(); Type type =
                   * binding.getType(item.getQualifier(),item.getName()); if
                   * (!(type instanceof NameType) && !(type instanceof
                   * FormalType)) { String msg = expForTypeMessage();
                   * err.error(item.getName().getLocation(), msg); break; } } }
                   */
                Binding bind = table.getCurrentBinding();
                Type type = new IndirectType(curArgument.getQualifier(), curArgument.getName(), bind);
                bind.addProgramIndirectName(curArgument.getQualifier(), curArgument.getName());
                typeMap.put(sym, type);
            }
        }
        return typeMap;
    }

    // -----------------------------------------------------------
    // Name creation
    // -----------------------------------------------------------

    private PosSymbol createInitName(Location loc) {
        Symbol sym = Symbol.symbol("%Init(" + loc.getPos().getLine() + "," + loc.getPos().getColumn() + ")");
        return new PosSymbol(loc, sym);
    }

    private PosSymbol createFinalName(Location loc) {
        Symbol sym = Symbol.symbol("%Final(" + loc.getPos().getLine() + "," + loc.getPos().getColumn() + ")");
        return new PosSymbol(loc, sym);
    }

    private PosSymbol createArrayName(Location loc) {
        Symbol sym = Symbol.symbol("%Array(" + loc.getPos().getLine() + "," + loc.getPos().getColumn() + ")");
        return new PosSymbol(loc, sym);
    }

    private PosSymbol createRecordName(Location loc) {
        Symbol sym =
                Symbol.symbol("%Record(" + loc.getPos().getLine() + "," + loc.getPos().getColumn() + ")");
        return new PosSymbol(loc, sym);
    }

    // -----------------------------------------------------------
    // Cast Methods
    // -----------------------------------------------------------

    private Dec castToDec(ModuleParameterDec par) {
        assert par instanceof Dec;
        return (Dec) par;
    }

    // -----------------------------------------------------------
    // Error Related Methods
    // -----------------------------------------------------------

    private String expForTypeMessage() {
        return "Cannot pass an expression to a module where a " + "type is required.";
    }

    private String moreParsThanArgsMessage() {
        return "The number of arguments is less than the number " + "of parameters.";
    }

    private String moreArgsThanParsMessage() {
        return "The number of arguments exceeds the number of parameters.";
    }

    private String cantFindFamilyMessage(String type) {
        return "Cant find the type family " + type + " in any of "
                + "the corresponding specification modules.";
    }
}
