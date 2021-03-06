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
 * SymbolTable.java
 * 
 * The Resolve Software Composition Workbench Project
 * 
 * Copyright (c) 1999-2005
 * Reusable Software Research Group
 * Department of Computer Science
 * Clemson University
 */

package edu.clemson.cs.r2jt.scope;

import edu.clemson.cs.r2jt.absyn.MathVarDec;
import edu.clemson.cs.r2jt.absyn.Ty;
import edu.clemson.cs.r2jt.analysis.TypeCorrespondence;
import edu.clemson.cs.r2jt.collections.Iterator;
import edu.clemson.cs.r2jt.collections.List;
import edu.clemson.cs.r2jt.collections.Map;
import edu.clemson.cs.r2jt.collections.Stack;
import edu.clemson.cs.r2jt.data.Location;
import edu.clemson.cs.r2jt.data.Mode;
import edu.clemson.cs.r2jt.data.ModuleID;
import edu.clemson.cs.r2jt.data.PosSymbol;
import edu.clemson.cs.r2jt.data.Symbol;
import edu.clemson.cs.r2jt.entry.*;
import edu.clemson.cs.r2jt.errors.ErrorHandler;
import edu.clemson.cs.r2jt.init.CompileEnvironment;
import edu.clemson.cs.r2jt.init.Environment;
import edu.clemson.cs.r2jt.location.DefinitionLocator;
import edu.clemson.cs.r2jt.location.SymbolSearchException;
import edu.clemson.cs.r2jt.type.Type;
import edu.clemson.cs.r2jt.type.TypeMatcher;

public class OldSymbolTable {

    // ==========================================================
    // Variables
    // ==========================================================

    private ModuleID id = null;

    /* Incremented each time a scope is named using this index. */
    private int scopeIndex = 1;

    private ErrorHandler err;
    //private Environment env = Environment.getInstance();
    CompileEnvironment myInstanceEnvironment;

    private Stack<Scope> stack = new Stack<Scope>();
    private List<Scope> list = new List<Scope>();
    private int listIndex = 0;

    /* Handle to the module level scope (bottom of stack). */
    private ModuleScope moduleScope = null;
    /* Handle to the current scope (top of stack). */
    private Scope currentScope = null;

    private String name = "Anonymous Table";

    // ==========================================================
    // Constructors
    // ==========================================================

    public OldSymbolTable(ModuleID id, CompileEnvironment instanceEnvironment) {
        myInstanceEnvironment = instanceEnvironment;
        this.id = id;
        this.err = instanceEnvironment.getErrorHandler();
    }

    public OldSymbolTable(String name, CompileEnvironment instanceEnvironment) {
        myInstanceEnvironment = instanceEnvironment;
        this.name = name;
        this.err = instanceEnvironment.getErrorHandler();
    }

    // ===========================================================
    // Accessors
    // ===========================================================

    public ModuleScope getModuleScope() {
        return moduleScope;
    }

    public Scope getCurrentScope() {
        return currentScope;
    }

    public Binding getBinding() {
        return moduleScope.getBinding();
    }

    public TypeHolder getTypeHolder() {
        return moduleScope.getTypeHolder();
    }

    public ModuleID getModuleID() {
        return id;
    }

    public Stack<Scope> getStack() {
        return stack;
    }

    public List<Scope> getList() {
        return list;
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    // -----------------------------------------------------------
    // Non-symbol Add Methods
    // -----------------------------------------------------------

    public void addModuleParameter(Entry entry) {
        assert moduleScope == currentScope;
        moduleScope.addModuleParameter(entry);
    }

    public void addConceptSpec(PosSymbol cName) {
        assert moduleScope == currentScope;
        ModuleID cid = ModuleID.createConceptID(cName);
        moduleScope.addSpec(cid);
    }

    public void addEnhancementSpec(PosSymbol eName, PosSymbol cName) {
        assert moduleScope == currentScope;
        ModuleID eid = ModuleID.createEnhancementID(eName, cName);
        moduleScope.addSpec(eid);
    }

    public void addAssocConcept(PosSymbol cName) {
        assert moduleScope == currentScope;
        ModuleID cid = ModuleID.createConceptID(cName);
        moduleScope.addAssociate(cid);
    }

    public void addAssocEnhancement(PosSymbol eName, PosSymbol cName) {
        assert moduleScope == currentScope;
        ModuleID eid = ModuleID.createEnhancementID(eName, cName);
        moduleScope.addAssociate(eid);
    }

    public void addAssocVisibleModules() {
        moduleScope.addAssocVisibleModules();
    }

    public void addUsesItem(PosSymbol name) {
        assert moduleScope == currentScope;
        ModuleID tid = ModuleID.createTheoryID(name);
        ModuleID fid = ModuleID.createFacilityID(name);
        if (myInstanceEnvironment.contains(tid)) {
            moduleScope.addUsesItem(tid);
        }
        else if (myInstanceEnvironment.contains(fid)) {
            moduleScope.addUsesItem(fid);
        }
        else {
            assert false : "addUsesItem failed";
        }
    }

    // -----------------------------------------------------------
    // Add To Scope Methods
    // -----------------------------------------------------------

    /** Adds the specified theorem to the current scope. */
    public void addTheoremToScope(TheoremEntry entry) {
        ModuleScope scope = castToModuleScope(currentScope);
        if (addPermitted(entry.getName())) {
            scope.addTheorem(entry);
        }
    }

    /** Adds the specified variable to the current scope. */
    public void addVariableToScope(VarEntry entry) {
        if (currentScope instanceof TypeScope && entry.getMode() != Mode.EXEMPLAR) {
            if (moduleScopeAddPermitted(entry.getName())) {
                moduleScope.addVariable(entry);
            }
        }
        else {
            if (addPermitted(entry.getName())) {
                currentScope.addVariable(entry);
            }
        }
    }

    /** Adds the specified operation to the current scope. */
    public void addOperationToScope(OperationEntry entry) {
        ModuleScope scope = castToModuleScope(currentScope);
        if (addPermitted(entry.getName())) {
            scope.addOperation(entry);
        }
    }

    private void setScopeForDefinition() {
        Stack<Scope> stackRev = new Stack<Scope>();
        while (!stack.isEmpty()) {
            Scope nextScope = stack.pop();
            stackRev.push(nextScope);
            if (nextScope instanceof ProofScope || nextScope instanceof ModuleScope) {
                currentScope = nextScope;
                break;
            }
        }
        while (!stackRev.isEmpty()) {
            stack.push(stackRev.pop());
        }
    }

    /** Adds the specified definition to the current scope. */
    public void addDefinitionToScope(DefinitionEntry entry) {
        Scope originalScope = currentScope;
        if (!(currentScope instanceof ProofScope) && !(currentScope instanceof ModuleScope)) {
            setScopeForDefinition();
        }
        if (currentScope instanceof ProofScope) {
            ProofScope scope = (ProofScope) currentScope;
            if (addPermitted(entry.getName())) {
                scope.addDefinition(entry);
                currentScope = originalScope;
                return;
            }
        }
        ModuleScope scope = castToModuleScope(currentScope);
        if (addPermitted(entry.getName())) {
            scope.addDefinition(entry);
        }
        currentScope = originalScope;
    }

    public void addProofToScope(ProofEntry entry) {
        ModuleScope scope = castToModuleScope(currentScope);
        if (addPermitted(entry.getName())) {
            scope.addProof(entry);
        }
    }

    /** Adds the specified type to the current scope. */
    public void addTypeToScope(TypeEntry entry) {
        if (currentScope instanceof ModuleScope) {
            ModuleScope scope = (ModuleScope) currentScope;
            if (addPermitted(entry.getName())) {
                scope.addType(entry);
                scope.getBinding().addTypeMapping(entry);
            }
        }
        else if (currentScope instanceof DefinitionScope) {
            DefinitionScope scope = (DefinitionScope) currentScope;
            if (addPermitted(entry.getName())) {
                scope.addType(entry);
                scope.getBinding().addTypeMapping(entry);
            }
        }
    }

    public void addDefinitionTypeToScope(TypeEntry entry) {
        if (currentScope instanceof ModuleScope) {
            ModuleScope s2 = (ModuleScope) currentScope;
            if (s2.addDefinitionTypePermitted(entry.getName().getSymbol())) {
                s2.addType(entry);
                s2.getBinding().addTypeMapping(entry);
            }
        }
        else if (currentScope instanceof ProofScope) {
            ProofScope s2 = (ProofScope) currentScope;
            if (s2.addDefinitionTypePermitted(entry.getName().getSymbol())) {
                s2.addType(entry);
                s2.getBinding().addTypeMapping(entry);
            }
        }
    }

    /** Adds the specified facility to the current scope. */
    public void addFacilityToScope(ModuleEntry entry) {
        if (currentScope instanceof ModuleScope) {
            ModuleScope scope = (ModuleScope) currentScope;
            if (addPermitted(entry.getName())) {
                scope.addFacility(entry);
            }
        }
        else if (currentScope instanceof ProcedureScope) {
            ProcedureScope scope = (ProcedureScope) currentScope;
            if (addPermitted(entry.getName())) {
                scope.addFacility(entry);
            }
        }
        else {
            assert false : "addFacility failed";
        }
    }

    public void addTypeCorrespondence(Type t1, Type t2) {
        moduleScope.addTypeCorrespondence(t1, t2);
    }

    public void addAlternateVarType(MathVarDec dec) {
        moduleScope.addAlternateVarType(dec);
    }

    public List<Type> getTypeCorrespondences(Type t, TypeMatcher tm) {
        List<TypeCorrespondence> equivTypes = getAllTypeCorrespondences();
        List<Type> types = new List<Type>();
        types.add(t);
        int origSize = 1;
        int skip = 0;
        while (true) {
            types.addAll(getTypeCorrespondencesFor(t, tm, equivTypes.iterator(), skip));
            if (types.size() == origSize) {
                break;
            }
            skip = origSize;
            origSize = types.size();
        }
        return types;
    }

    private List<TypeCorrespondence> getAllTypeCorrespondences() {
        List<TypeCorrespondence> correspondences = new List<TypeCorrespondence>();
        correspondences.addAll(moduleScope.getTypeCorrespondences());
        Iterator<ModuleScope> scopeIt = moduleScope.getMathVisibleModules();
        while (scopeIt.hasNext()) {
            correspondences.addAll(scopeIt.next().getTypeCorrespondences());
        }
        return correspondences;
    }

    private List<Type> getTypeCorrespondencesFor(Type t, TypeMatcher tm,
            Iterator<TypeCorrespondence> correspondences, int skip) {
        List<Type> typesFound = new List<Type>();
        TypeCorrespondence temp = null;
        for (int i = 0; i < skip; i++) {
            correspondences.next();
        }
        while (correspondences.hasNext()) {
            temp = correspondences.next();
            if (tm.mathMatches(t, temp.getType1())) {
                typesFound.add(temp.getType2());
            }
        }
        return typesFound;
    }

    // -----------------------------------------------------------
    // Binding Methods
    // -----------------------------------------------------------

    public Binding getCurrentBinding() {
        Binding result = null;
        Stack<Scope> stack2 = new Stack<Scope>();
        while (stack.size() > 0) {
            Scope scope = stack.pop();
            stack2.push(scope);
            if (scope instanceof ProcedureScope) {
                result = ((ProcedureScope) scope).getBinding();
                break;
            }
            if (scope instanceof ProofScope) {
                result = ((ProofScope) scope).getBinding();
                break;
            }
            if (scope instanceof ModuleScope) {
                result = ((ModuleScope) scope).getBinding();
                break;
            }
        }
        while (stack2.size() > 0) {
            stack.push(stack2.pop());
        }
        return result;
    }

    public void bindTypeNames() {
        if (moduleScope != null) {
            moduleScope.getBinding().bindTypeNames();
            moduleScope.getTypeHolder().searchForBuiltInTypes();
        }
    }

    public void bindProcedureTypeNames() {
        if (currentScope instanceof ProcedureScope) {
            ProcedureScope scope = (ProcedureScope) currentScope;
            scope.getBinding().bindTypeNames();
        }
        else {
            assert false : "bindProcedureTypeNames failed";
        }
    }

    public void bindProofTypeNames() {
        if (currentScope instanceof ProofScope) {
            ProofScope scope = (ProofScope) currentScope;
            scope.getBinding().bindTypeNames();
        }
        else {
            assert false : "bindProofTypeNames failed";
        }
    }

    // -----------------------------------------------------------
    // Create Scope Methods
    // -----------------------------------------------------------

    public void createModuleScope() {
        ModuleScope scope = new ModuleScope(id, myInstanceEnvironment);
        createNewScope(scope);
        moduleScope = scope;
        assert stack.size() == 1;
        assert list.size() == 1;
    }

    public void createTypeScope() {
        ScopeID sid = ScopeID.createTypeScopeID(id, scopeIndex);
        scopeIndex++;
        TypeScope scope = new TypeScope(sid);
        createNewScope(scope);
    }

    public void createOperationScope(PosSymbol name) {
        ModuleID mid = myInstanceEnvironment.getModuleID(name.getFile());
        ScopeID sid = ScopeID.createOperationScopeID(name, mid);
        OperationScope scope = new OperationScope(sid);
        createNewScope(scope);
    }

    public void createProcedureScope(PosSymbol name) {
        ModuleID mid = myInstanceEnvironment.getModuleID(name.getFile());
        ScopeID sid = ScopeID.createProcedureScopeID(name, mid);
        ProcedureScope scope = new ProcedureScope(moduleScope, sid, myInstanceEnvironment);
        createNewScope(scope);
    }

    public void createProofScope(PosSymbol name) {
        ScopeID sid = ScopeID.createProofScopeID(name);
        ProofScope scope = new ProofScope(moduleScope, sid, myInstanceEnvironment);
        createNewScope(scope);
    }

    public void createDefinitionScope(PosSymbol name) {
        ScopeID sid = ScopeID.createDefinitionScopeID(name);
        DefinitionScope scope = new DefinitionScope(sid, myInstanceEnvironment);
        createNewScope(scope);
    }

    public void createStatementScope() {
        ScopeID sid = ScopeID.createStatementScopeID(id, scopeIndex);
        scopeIndex++;
        StatementScope scope = new StatementScope(sid);
        createNewScope(scope);
    }

    public void createExpressionScope() {
        ScopeID sid = ScopeID.createExpressionScopeID(id, scopeIndex);
        scopeIndex++;
        ExpressionScope scope = new ExpressionScope(sid);
        createNewScope(scope);
    }

    // -----------------------------------------------------------
    // Complete Scope Methods
    // -----------------------------------------------------------

    public void completeModuleScope() {
        assert stack.getTop() instanceof ModuleScope;
        completeNewScope();
        assert stack.size() == 0;
    }

    public void completeTypeScope() {
        assert stack.getTop() instanceof TypeScope;
        completeNewScope();
    }

    public void completeOperationScope() {
        assert stack.getTop() instanceof OperationScope;
        completeNewScope();
    }

    public void completeProofScope() {
        assert stack.getTop() instanceof ProofScope;
        completeNewScope();
    }

    public void completeProcedureScope() {
        assert stack.getTop() instanceof ProcedureScope;
        completeNewScope();
    }

    public void completeDefinitionScope() {
        assert stack.getTop() instanceof DefinitionScope;
        completeNewScope();
    }

    public void completeStatementScope() {
        assert stack.getTop() instanceof StatementScope;
        completeNewScope();
    }

    public void completeExpressionScope() {
        assert stack.getTop() instanceof ExpressionScope;
        completeNewScope();
    }

    // -----------------------------------------------------------
    // Begin Scope Methods
    // -----------------------------------------------------------

    public void beginModuleScope() {
        beginScope();
        assert currentScope instanceof ModuleScope;
        assert stack.size() == 1;
    }

    //      public void beginTypeScope() {
    //          beginScope();
    //          assert currentScope instanceof TypeScope;
    //      }

    //      public void beginOperationScope() {
    //          beginScope();
    //          assert currentScope instanceof OperationScope;
    //      }

    //      public void beginProcedureScope() {
    //          beginScope();
    //          assert currentScope instanceof ProcedureScope;
    //      }

    //      public void beginDefinitionScope() {
    //          beginScope();
    //          assert currentScope instanceof DefinitionScope;
    //      }

    //      public void beginStatementScope() {
    //          beginScope();
    //          assert currentScope instanceof StatementScope;
    //      }

    //      public void beginExpressionScope() {
    //          beginScope();
    //          assert currentScope instanceof ExpressionScope;
    //      }

    public void beginTypeScope() {
        Scope scope = list.get(listIndex);
        listIndex++;
        while (!(scope instanceof TypeScope)) {
            assert listIndex < list.size();
            scope = list.get(listIndex);
            listIndex++;
        }
        stack.push(scope);
        currentScope = scope;
    }

    public void beginOperationScope() {
        Scope scope = list.get(listIndex);
        listIndex++;
        while (!(scope instanceof OperationScope)) {
            assert listIndex < list.size();
            scope = list.get(listIndex);
            listIndex++;
        }
        stack.push(scope);
        currentScope = scope;
    }

    public void beginProofScope() {
        Scope scope = list.get(listIndex);
        listIndex++;
        while (!(scope instanceof ProofScope)) {
            assert listIndex < list.size();
            scope = list.get(listIndex);
            listIndex++;
        }
        stack.push(scope);
        currentScope = scope;
    }

    public void beginProcedureScope() {
        Scope scope = list.get(listIndex);
        listIndex++;
        while (!(scope instanceof ProcedureScope)) {
            assert listIndex < list.size();
            scope = list.get(listIndex);
            listIndex++;
        }
        stack.push(scope);
        currentScope = scope;
    }

    public void beginDefinitionScope() {
        Scope scope = list.get(listIndex);
        listIndex++;
        while (!(scope instanceof DefinitionScope)) {
            assert listIndex < list.size();
            scope = list.get(listIndex);
            listIndex++;
        }
        stack.push(scope);
        currentScope = scope;
    }

    public void beginStatementScope() {
        Scope scope = list.get(listIndex);
        listIndex++;
        while (!(scope instanceof StatementScope)) {
            assert listIndex < list.size();
            scope = list.get(listIndex);
            listIndex++;
        }
        stack.push(scope);
        currentScope = scope;
    }

    public void beginExpressionScope() {
        Scope scope = list.get(listIndex);
        listIndex++;
        while (!(scope instanceof ExpressionScope)) {
            assert listIndex < list.size();
            scope = list.get(listIndex);
            listIndex++;
        }
        stack.push(scope);
        currentScope = scope;
    }

    // -----------------------------------------------------------
    // End Scope Methods
    // -----------------------------------------------------------

    public void endModuleScope() {
        assert stack.getTop() instanceof ModuleScope;
        endScope();
        listIndex = 0;
        assert stack.size() == 0;
    }

    public void endTypeScope() {
        assert stack.getTop() instanceof TypeScope;
        endScope();
    }

    public void endOperationScope() {
        assert stack.getTop() instanceof OperationScope;
        endScope();
    }

    public void endProofScope() {
        assert stack.getTop() instanceof ProofScope;
        endScope();
    }

    public void endProcedureScope() {
        assert stack.getTop() instanceof ProcedureScope;
        endScope();
    }

    public void endDefinitionScope() {
        assert stack.getTop() instanceof DefinitionScope;
        endScope();
    }

    public void endStatementScope() {
        assert stack.getTop() instanceof StatementScope;
        endScope();
    }

    public void endExpressionScope() {
        assert stack.getTop() instanceof ExpressionScope;
        endScope();
    }

    // -----------------------------------------------------------
    // Short Facility Method
    // -----------------------------------------------------------

    public void createShortFacility(PosSymbol name) {
        assert moduleScope != null : "moduleScope is null";
        assert moduleScope.isProgramVisible(name.getSymbol());
        ModuleScope facscope = moduleScope.getProgramVisibleModule(name.getSymbol());
        moduleScope.removeFacilityFromVisible(name.getSymbol());
        facscope.simplifyNames(name);
        moduleScope.setFacbind(facscope.getBinding());
        moduleScope.mergeFacility(facscope);
    }

    // -----------------------------------------------------------
    // Print Methods
    // -----------------------------------------------------------

    public String printStack() {
        StringBuffer sb = new StringBuffer();
        Stack<Scope> hold = new Stack<Scope>();
        sb.append("ScopeStack( ");
        while (!stack.isEmpty()) {
            Scope scope = stack.pop();
            sb.append(scope.getScopeID().toString());
            hold.push(scope);
            if (!stack.isEmpty()) {
                sb.append(", ");
            }
        }
        sb.append(")");
        while (!hold.isEmpty()) {
            stack.push(hold.pop());
        }
        return sb.toString();
    }

    public String printList() {
        StringBuffer sb = new StringBuffer();
        sb.append("ScopeList[");
        sb.append(listIndex + "]( ");
        Iterator<Scope> i = list.iterator();
        while (i.hasNext()) {
            Scope scope = i.next();
            sb.append(scope.getScopeID().toString());
            if (i.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    // ===========================================================
    // Private Methods
    // ===========================================================

    // -----------------------------------------------------------
    // Cast Methods
    // -----------------------------------------------------------

    private ModuleScope castToModuleScope(Scope scope) {
        assert scope instanceof ModuleScope;
        return (ModuleScope) scope;
    }

    private OperationScope castToOperationScope(Scope scope) {
        assert scope instanceof OperationScope;
        return (OperationScope) scope;
    }

    private TypeScope castToTypeScope(Scope scope) {
        assert scope instanceof TypeScope;
        return (TypeScope) scope;
    }

    // -----------------------------------------------------------
    // Add Permitted Methods
    // -----------------------------------------------------------

    private boolean addPermitted(PosSymbol name) {
        if (currentScope.addPermitted(name.getSymbol())) {
            return true;
        }
        else {
            Entry entry = currentScope.getAddObstructor(name.getSymbol());
            String msg = alreadyDefinedMessage(entry.getLocation().toString());
            err.error(name.getLocation(), msg);
            return false;
        }
    }

    private boolean moduleScopeAddPermitted(PosSymbol name) {
        if (moduleScope.addPermitted(name.getSymbol())) {
            return true;
        }
        else {
            Entry entry = moduleScope.getAddObstructor(name.getSymbol());
            String msg = alreadyDefinedMessage(entry.getLocation().toString());
            err.error(name.getLocation(), msg);
            return false;
        }
    }

    // -----------------------------------------------------------
    // Scope Start and Stop Methods
    // -----------------------------------------------------------

    private void createNewScope(Scope scope) {
        stack.push(scope);
        list.add(scope);
        currentScope = scope;
    }

    private void completeNewScope() {
        stack.pop();
        currentScope = stack.getTop();
    }

    private void beginScope() {
        Scope scope = list.get(listIndex);
        listIndex++;
        stack.push(scope);
        currentScope = scope;
    }

    private void endScope() {
        stack.pop();
        currentScope = stack.getTop();
    }

    // -----------------------------------------------------------
    // Error Related Methods
    // -----------------------------------------------------------

    private String alreadyDefinedMessage(String loc) {
        return "Symbol already defined in current scope (at " + loc + ")";
    }

    private String alreadyModuleDefinedMessage(String loc) {
        return "Symbol already defined in module scope (at " + loc + ")";
    }
}
