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
 * SetExp.java
 * 
 * The Resolve Software Composition Workbench Project
 * 
 * Copyright (c) 1999-2005
 * Reusable Software Research Group
 * Department of Computer Science
 * Clemson University
 */

package edu.clemson.cs.r2jt.absyn;

import edu.clemson.cs.r2jt.collections.List;
import edu.clemson.cs.r2jt.data.Location;
import edu.clemson.cs.r2jt.type.Type;
import edu.clemson.cs.r2jt.analysis.TypeResolutionException;

public class SetExp extends Exp {

    // ===========================================================
    // Variables
    // ===========================================================

    /** The location member. */
    private Location location;

    /** The var member. */
    private MathVarDec var;

    /** The where member. */
    private Exp where;

    /** The body member. */
    private Exp body;

    /** List of variables */
    private List<VarExp> vars;

    // ===========================================================
    // Constructors
    // ===========================================================

    public SetExp() {};

    public SetExp(Location location, MathVarDec var, Exp where, Exp body) {
        this.location = location;
        this.var = var;
        this.where = where;
        this.body = body;
    }

    public SetExp(Location location, MathVarDec var, Exp where, Exp body, List<VarExp> vars) {
        this.location = location;
        this.var = var;
        this.where = where;
        this.body = body;
        this.vars = vars;
    }

    public Exp substituteChildren(java.util.Map<Exp, Exp> substitutions) {
        return new SetExp(location, var, substitute(where, substitutions), substitute(body, substitutions),
                vars);
    }

    // ===========================================================
    // Accessor Methods
    // ===========================================================

    // -----------------------------------------------------------
    // Get Methods
    // -----------------------------------------------------------

    /** Returns the value of the location variable. */
    public Location getLocation() {
        return location;
    }

    /** Returns the value of the var variable. */
    public MathVarDec getVar() {
        return var;
    }

    /** Returns the value of the where variable. */
    public Exp getWhere() {
        return where;
    }

    /** Returns the value of the body variable. */
    public Exp getBody() {
        return body;
    }

    /** Returns the list of the variables. */
    public List<VarExp> getVars() {
        return vars;
    }

    // -----------------------------------------------------------
    // Set Methods
    // -----------------------------------------------------------

    /** Sets the location variable to the specified value. */
    public void setLocation(Location location) {
        this.location = location;
    }

    /** Sets the var variable to the specified value. */
    public void setVar(MathVarDec var) {
        this.var = var;
    }

    /** Sets the where variable to the specified value. */
    public void setWhere(Exp where) {
        this.where = where;
    }

    /** Sets the body variable to the specified value. */
    public void setBody(Exp body) {
        this.body = body;
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    /** Accepts a ResolveConceptualVisitor. */
    public void accept(ResolveConceptualVisitor v) {
        v.visitSetExp(this);
    }

    /** Accepts a TypeResolutionVisitor. */
    public Type accept(TypeResolutionVisitor v) throws TypeResolutionException {
        return v.getSetExpType(this);
    }

    /** Returns a formatted text string of this class. */
    public String asString(int indent, int increment) {

        StringBuffer sb = new StringBuffer();

        printSpace(indent, sb);
        sb.append("SetExp\n");

        if (var != null) {
            sb.append(var.asString(indent + increment, increment));
        }

        if (where != null) {
            sb.append(where.asString(indent + increment, increment));
        }

        if (body != null) {
            sb.append(body.asString(indent + increment, increment));
        }

        return sb.toString();
    }

    /** Returns true if the variable is found in any sub expression
        of this one. **/
    public boolean containsVar(String varName, boolean IsOldExp) {
        Boolean found = false;
        if (where != null) {
            found = where.containsVar(varName, IsOldExp);
        }
        if (!found && body != null) {
            found = body.containsVar(varName, IsOldExp);
        }
        return found;
    }

    public List<Exp> getSubExpressions() {
        List<Exp> list = new List<Exp>();
        list.add(where);
        list.add(body);
        return list;
    }

    public void setSubExpression(int index, Exp e) {
        switch (index) {
        case 0:
            where = e;
            break;
        case 1:
            body = e;
            break;
        }
    }

    public boolean shallowCompare(Exp e2) {
        if (!(e2 instanceof SetExp)) {
            return false;
        }
        return true;
    }

    public void prettyPrint() {
        System.out.print("{ ");
        var.prettyPrint();
        System.out.print(", ");
        if (where != null) {
            where.prettyPrint();
            System.out.print(", ");
        }
        body.prettyPrint();
        System.out.print(" }");
    }

    public Exp copy() {
        MathVarDec newVar = var.copy();
        List<VarExp> newVars = new List<VarExp>();

        for (VarExp v : vars) {
            newVars.add((VarExp) Exp.copy(v));
        }

        Exp newWhere = null;
        if (where != null)
            newWhere = Exp.copy(where);
        Exp newBody = Exp.copy(body);
        return new SetExp(null, newVar, newWhere, newBody, newVars);
    }

}
