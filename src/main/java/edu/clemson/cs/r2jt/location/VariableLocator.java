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
 * VariableLocator.java
 * 
 * The Resolve Software Composition Workbench Project
 * 
 * Copyright (c) 1999-2005
 * Reusable Software Research Group
 * Department of Computer Science
 * Clemson University
 */

package edu.clemson.cs.r2jt.location;

import edu.clemson.cs.r2jt.absyn.Exp;
import edu.clemson.cs.r2jt.absyn.VarExp;
import edu.clemson.cs.r2jt.collections.Iterator;
import edu.clemson.cs.r2jt.collections.List;
import edu.clemson.cs.r2jt.collections.Stack;
import edu.clemson.cs.r2jt.data.Location;
import edu.clemson.cs.r2jt.data.ModuleID;
import edu.clemson.cs.r2jt.data.PosSymbol;
import edu.clemson.cs.r2jt.data.Symbol;
import edu.clemson.cs.r2jt.entry.*;
import edu.clemson.cs.r2jt.errors.ErrorHandler;
import edu.clemson.cs.r2jt.init.Environment;
import edu.clemson.cs.r2jt.scope.*;
import edu.clemson.cs.r2jt.type.*;

public class VariableLocator {

    // ===========================================================
    // Variables
    // ===========================================================

    private ErrorHandler err;

    //private Environment env = Environment.getInstance();

    private boolean showErrors = true;

    private boolean local = false;

    private OldSymbolTable table;

    // ===========================================================
    // Constructors
    // ===========================================================

    public VariableLocator(OldSymbolTable table, ErrorHandler eh) {
        this.table = table;
        this.err = eh;
    }

    public VariableLocator(OldSymbolTable table, boolean err, ErrorHandler eh) {
        this.table = table;
        showErrors = err;
        this.err = eh;
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    public void setErrors(boolean flag) {
        showErrors = flag;
    }

    //      public boolean isArrayVariable(PosSymbol qual, PosSymbol name)
    //          throws SymbolSearchException
    //      {
    //          if (qual == null) {
    //              List<VarEntry> vars = locateVariablesInStack(name);
    //              if (vars.size() == 0) {
    //                  vars = locateProgramVariablesInImports(name);
    //              }
    //              if (vars.size() > 0) {
    //                  return true;
    //              } else {
    //                  return false;
    //              }
    //          } else {
    //              QualifierLocator qlocator = new QualifierLocator(table);
    //              ModuleScope scope = qlocator.locateProgramModule(qual);
    //              if (scope.containsVariable(name.getSymbol())) {
    //                  return true;
    //              } else {
    //                  return false;
    //              }
    //          }
    //      }

    public boolean isArrayVariable(PosSymbol qual, PosSymbol name) {
        try {
            if (qual == null) {
                List<VarEntry> vars = locateVariablesInStack(name);
                if (vars.size() == 0) {
                    vars = locateProgramVariablesInImports(name);
                }
                if (vars.size() > 0) {
                    Iterator<VarEntry> i = vars.iterator();
                    while (i.hasNext()) {
                        VarEntry var = i.next();
                        if (isArrayType(var.getType())) {
                            return true;
                        }
                    }
                }
                return false;
            }
            else {
                QualifierLocator qlocator = new QualifierLocator(table, err);
                ModuleScope scope = qlocator.locateProgramModule(qual);
                if (scope.containsVariable(name.getSymbol())) {
                    VarEntry var = scope.getVariable(name.getSymbol());
                    if (isArrayType(var.getType())) {
                        return true;
                    }
                }
                return false;
            }
        }
        catch (SymbolSearchException sse) {
            return false;
        }
    }

    public VarEntry locateMathVariable(PosSymbol qual, PosSymbol name, Exp exp) throws SymbolSearchException {
        local = false;
        VarEntry ve = locateMathVariable(qual, name);
        if (local && (exp instanceof VarExp)) {
            ((VarExp) exp).setIsLocal(true);
        }
        return ve;
    }

    public VarEntry locateProgramVariable(PosSymbol qual, PosSymbol name, Exp exp)
            throws SymbolSearchException {
        local = false;
        VarEntry ve = locateProgramVariable(qual, name);
        if (local && (exp instanceof VarExp)) {
            ((VarExp) exp).setIsLocal(true);
        }
        return ve;
    }

    public VarEntry locateMathVariable(PosSymbol name) throws SymbolSearchException {
        List<VarEntry> vars = locateVariablesInStack(name);
        if (vars.size() == 0) {
            vars = locateMathVariablesInImports(name);
        }
        return getUniqueMathVariable(name, vars);
    }

    public VarEntry locateProgramVariable(PosSymbol name) throws SymbolSearchException {
        List<VarEntry> vars = locateVariablesInStack(name);
        if (vars.size() == 0) {
            vars = locateProgramVariablesInImports(name);
        }
        return getUniqueProgramVariable(name, vars);
    }

    public VarEntry locateMathVariable(PosSymbol qual, PosSymbol name) throws SymbolSearchException {
        if (qual == null) {
            return locateMathVariable(name);
        }
        QualifierLocator qlocator = new QualifierLocator(table, err);
        ModuleScope scope = qlocator.locateMathModule(qual);
        if (scope.containsVariable(name.getSymbol())) {
            return scope.getVariable(name.getSymbol());
        }
        else {
            if (showErrors) {
                String msg = cantFindVarInModMessage(name.toString(), qual.toString());
                err.error(qual.getLocation(), msg);
            }
            throw new SymbolSearchException();
        }
    }

    public VarEntry locateProgramVariable(PosSymbol qual, PosSymbol name) throws SymbolSearchException {
        if (qual == null) {
            return locateProgramVariable(name);
        }
        QualifierLocator qlocator = new QualifierLocator(table, err);
        ModuleScope scope = qlocator.locateProgramModule(qual);
        if (scope.containsVariable(name.getSymbol())) {
            VarEntry var = scope.getVariable(name.getSymbol());
            checkProgramVariable(name, var);
            return var;
        }
        else {
            if (showErrors) {
                String msg = cantFindVarInModMessage(name.toString(), qual.toString());
                err.error(qual.getLocation(), msg);
            }
            throw new SymbolSearchException();
        }
    }

    // -----------------------------------------------------------
    // Location Methods
    // -----------------------------------------------------------

    public List<VarEntry> locateVariablesInStack(PosSymbol name) throws SymbolSearchException {
        List<VarEntry> vars = new List<VarEntry>();
        Stack<Scope> stack = table.getStack();
        Stack<Scope> hold = new Stack<Scope>();
        try {
            while (!stack.isEmpty()) {
                Scope scope = stack.pop();
                hold.push(scope);
                if (scope.containsVariable(name.getSymbol())) {
                    vars.add(scope.getVariable(name.getSymbol()));
                    // if you find the variable in a procedure, mark it local (for the proof checker)
                    if (scope instanceof ProcedureScope || scope instanceof DefinitionScope
                            || scope instanceof OperationScope || scope instanceof ExpressionScope) {
                        local = true;
                    }
                    break;
                }
                if (scope instanceof ProcedureScope) {
                    vars.addAll(locateVariablesInProc(name, (ProcedureScope) scope));
                    if (vars.size() > 0) {
                        break;
                    }
                }
                if (scope instanceof ProofScope) {
                    vars.addAll(locateVariablesInProof(name, (ProofScope) scope));
                    if (vars.size() > 0) {
                        break;
                    }
                }
            }
            return vars;
        }
        finally {
            while (!hold.isEmpty()) {
                stack.push(hold.pop());
            }
        }
    }

    public List<VarEntry> locateMathVariablesInImports(PosSymbol name) throws SymbolSearchException {
        List<VarEntry> vars = new List<VarEntry>();
        Iterator<ModuleScope> i = table.getModuleScope().getMathVisibleModules();
        while (i.hasNext()) {
            ModuleScope iscope = i.next();
            if (iscope.containsVariable(name.getSymbol())) {
                vars.add(iscope.getVariable(name.getSymbol()));
            }
        }
        return vars;
    }

    // ===========================================================
    // Private Methods
    // ===========================================================

    private boolean isArrayType(Type type) {
        while (type instanceof IndirectType) {
            type = ((IndirectType) type).getType();
        }
        return (type instanceof ArrayType);
    }

    // -----------------------------------------------------------
    // General Variable Location Methods
    // -----------------------------------------------------------

    private List<VarEntry> locateVariablesInProc(PosSymbol name, ProcedureScope scope)
            throws SymbolSearchException {
        List<VarEntry> vars = new List<VarEntry>();
        Iterator<ModuleScope> i = scope.getVisibleModules();
        while (i.hasNext()) {
            ModuleScope iscope = i.next();
            if (iscope.containsVariable(name.getSymbol())) {
                vars.add(iscope.getVariable(name.getSymbol()));
            }
        }
        return vars;
    }

    private List<VarEntry> locateVariablesInProof(PosSymbol name, ProofScope scope)
            throws SymbolSearchException {
        List<VarEntry> vars = new List<VarEntry>();
        Iterator<ModuleScope> i = scope.getVisibleModules();
        while (i.hasNext()) {
            ModuleScope iscope = i.next();
            if (iscope.containsVariable(name.getSymbol())) {
                vars.add(iscope.getVariable(name.getSymbol()));
            }
        }
        return vars;
    }

    // -----------------------------------------------------------
    // Unqualified Math Variable Methods
    // -----------------------------------------------------------

    private VarEntry getUniqueMathVariable(PosSymbol name, List<VarEntry> vars) throws SymbolSearchException {
        if (vars.size() == 0) {
            String msg = cantFindVarMessage(name.toString());
            throw new SymbolSearchException(msg);
        }
        else if (vars.size() == 1) {
            return vars.get(0);
        }
        else { // vars.size() > 1
            List<Location> locs = getLocationList(vars);
            if (showErrors) {
                String msg = ambigVarRefMessage(name.toString(), locs.toString());
                err.error(name.getLocation(), msg);
            }
            throw new SymbolSearchException();
        }
    }

    // -----------------------------------------------------------
    // Unqualified Program Variable Methods
    // -----------------------------------------------------------

    private List<VarEntry> locateProgramVariablesInImports(PosSymbol name) throws SymbolSearchException {
        List<VarEntry> vars = new List<VarEntry>();
        Iterator<ModuleScope> i = table.getModuleScope().getProgramVisibleModules();
        while (i.hasNext()) {
            ModuleScope iscope = i.next();
            if (iscope.containsVariable(name.getSymbol())) {
                vars.add(iscope.getVariable(name.getSymbol()));
            }
        }
        return vars;
    }

    private VarEntry getUniqueProgramVariable(PosSymbol name, List<VarEntry> vars)
            throws SymbolSearchException {
        if (vars.size() == 0) {
            if (showErrors) {
                String msg = cantFindProgVarMessage(name.toString());
                err.error(name.getLocation(), msg);
            }
            throw new SymbolSearchException();
        }
        else if (vars.size() == 1) {
            checkProgramVariable(name, vars.get(0));
            return vars.get(0);
        }
        else { // vars.size() > 1
            return disambiguateProgramVariables(name, vars);
        }
    }

    private VarEntry disambiguateProgramVariables(PosSymbol name, List<VarEntry> vars)
            throws SymbolSearchException {
        List<VarEntry> newvars = new List<VarEntry>();
        Iterator<VarEntry> i = vars.iterator();
        while (i.hasNext()) {
            VarEntry var = i.next();
            if (!isProgramVariable(var)) {
                newvars.add(var);
            }
        }
        if (newvars.size() == 0) {
            List<Location> locs = getLocationList(vars);
            if (showErrors) {
                String msg = cantFindProgVarMessage(name.toString(), locs.toString());
                err.error(name.getLocation(), msg);
            }
            throw new SymbolSearchException();
        }
        else if (newvars.size() == 1) {
            return newvars.get(0);
        }
        else { // newvars.size() > 1
            List<Location> locs = getLocationList(vars);
            if (showErrors) {
                String msg = ambigVarRefMessage(name.toString(), locs.toString());
                err.error(name.getLocation(), msg);
            }
            throw new SymbolSearchException();
        }
    }

    private void checkProgramVariable(PosSymbol name, VarEntry var) throws SymbolSearchException {
        if (!isProgramVariable(var)) {
            if (showErrors) {
                String msg = notProgVarMessage(var.getName().toString());
                err.error(name.getLocation(), msg);
            }
            throw new SymbolSearchException();
        }
    }

    private boolean isProgramVariable(VarEntry var) {
        assert var != null : "var is null";
        Type type = var.getType();
        while (type instanceof IndirectType) {
            type = ((IndirectType) type).getType();
            if (var.getMode().toString().equals("Local")) {
                return true;
            }
        }
        return (type instanceof FormalType || type instanceof ConcType || type instanceof NameType
                || type instanceof ArrayType || type instanceof RecordType);
    }

    private List<Location> getLocationList(List<VarEntry> entries) {
        List<Location> locs = new List<Location>();
        Iterator<VarEntry> i = entries.iterator();
        while (i.hasNext()) {
            VarEntry entry = i.next();
            locs.add(entry.getLocation());
        }
        return locs;
    }

    // -----------------------------------------------------------
    // Error Related Methods
    // -----------------------------------------------------------

    private String cantFindVarInModMessage(String name, String module) {
        return "Cannot find a variable named " + name + " in module " + module + ".";
    }

    private String ambigVarRefMessage(String name, String mods) {
        return "The variable named " + name + " is found in more than one "
                + "module visible from this scope: " + mods + ".";
    }

    private String cantFindProgVarMessage(String name) {
        return "Cannot find a program variable named " + name + ".";
    }

    private String cantFindProgVarMessage(String name, String mods) {
        return "Cannot find a program variable named " + name + ", but found math variables: " + mods + ".";
    }

    private String cantFindVarMessage(String name) {
        return "Cannot find a variable named " + name + ".";
    }

    private String notProgVarMessage(String name) {
        return "The variable " + name + " is not a program variable.";
    }

}
