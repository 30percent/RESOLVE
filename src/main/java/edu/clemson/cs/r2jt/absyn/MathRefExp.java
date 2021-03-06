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
 * MathRefExp.java
 * 
 * The Resolve Software Composition Workbench Project
 * 
 * Copyright (c) 1999-2006
 * Reusable Software Research Group
 * Department of Computer Science
 * Clemson University
 */

package edu.clemson.cs.r2jt.absyn;

import edu.clemson.cs.r2jt.collections.Iterator;
import edu.clemson.cs.r2jt.collections.List;
import edu.clemson.cs.r2jt.data.Location;
import edu.clemson.cs.r2jt.type.Type;
import edu.clemson.cs.r2jt.analysis.TypeResolutionException;
import edu.clemson.cs.r2jt.data.PosSymbol;

public class MathRefExp extends Exp {

    // ===========================================================
    // Constants
    // ===========================================================

    public static final int AXIOM = 1;
    public static final int THEOREM = 2;
    public static final int PROPERTY = 3;
    public static final int LEMMA = 4;
    public static final int COROLLARY = 5;
    public static final int SUPPOSITION = 6;
    public static final int LINEREF = 7;
    public static final int DEFINITION = 8;
    public static final int DEDUCTION = 9;
    public static final int SELF = 10;

    // ===========================================================
    // Variables
    // ===========================================================

    /** The location member. */
    private Location location;

    /** The kind member. */
    private int kind;

    /** The id member. */
    private PosSymbol id;

    /** The index of the rule within the definition if the id identifies an 
     *  inductive definition. */
    private PosSymbol index;

    /** The name of the module in which we will find the id. */
    private PosSymbol sourceModule;

    private List<VarExp> params;

    // ===========================================================
    // Constructors
    // ===========================================================

    public MathRefExp() {};

    public MathRefExp(Location location, int kind, PosSymbol id) {
        this.location = location;
        this.kind = kind;
        this.id = id;
        this.params = null;
    }

    public MathRefExp(Location location, int kind, PosSymbol id, List<VarExp> params) {
        this.location = location;
        this.kind = kind;
        this.id = id;
        this.params = params;
    }

    public MathRefExp(Location location, int kind, PosSymbol id, PosSymbol index, PosSymbol sourceModule,
            List<VarExp> params) {
        this.location = location;
        this.kind = kind;
        this.id = id;
        this.index = index;
        this.sourceModule = sourceModule;
        this.params = params;
    }

    public Exp substituteChildren(java.util.Map<Exp, Exp> substitutions) {
        List<VarExp> newParams = new List<VarExp>();
        for (VarExp v : params) {
            newParams.add((VarExp) substitute(v, substitutions));
        }

        return new MathRefExp(location, kind, id, index, sourceModule, newParams);
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

    /** Returns the value of the kind variable. */
    public int getKind() {
        return kind;
    }

    /** Returns the value of the id variable. */
    public PosSymbol getId() {
        return id;
    }

    public PosSymbol getSourceModule() {
        return sourceModule;
    }

    public List<VarExp> getParams() {
        return params;
    }

    public PosSymbol getIndex() {
        return index;
    }

    // -----------------------------------------------------------
    // Set Methods
    // -----------------------------------------------------------

    /** Sets the value of the location variable. */
    public void setLocation(Location location) {
        this.location = location;
    }

    /** Sets the value of the mathExp1 variable. */
    public void setKind(int kind) {
        this.kind = kind;
    }

    /** Sets the value of the mathExp2 variable. */
    public void setId(PosSymbol id) {
        this.id = id;
    }

    public void setParams(List<VarExp> params) {
        this.params = params;
    }

    public void setIndex(PosSymbol index) {
        this.index = index;
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    /** Accepts a ResolveConceptualVisitor. */
    public void accept(ResolveConceptualVisitor v) {
        v.visitMathRefExp(this);
    }

    /** Accepts a TypeResolutionVisitor. */
    public Type accept(TypeResolutionVisitor v) throws TypeResolutionException {
        return v.getMathRefExpType(this);
    }

    /** Returns a formatted text string of this class. */
    public String asString(int indent, int increment) {

        StringBuffer sb = new StringBuffer();

        printSpace(indent, sb);
        sb.append("MathRefExp\n");

        printSpace(indent + increment, sb);
        sb.append("Kind: " + kind);
        sb.append("\n");

        if (!(index == null && id == null && sourceModule == null)) {
            printSpace(indent + increment, sb);
            sb.append("By ");

            if (index != null) {
                sb.append("(" + index.getName() + ") of ");
            }

            if (id != null) {
                sb.append(id.getName());
            }

            if (sourceModule != null) {
                sb.append(" from " + sourceModule.getName());
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    public boolean containsVar(String varName, boolean IsOldExp) {
        Iterator<VarExp> paramsIt = params.iterator();
        while (paramsIt.hasNext()) {
            if (paramsIt.next().containsVar(varName, IsOldExp)) {
                return true;
            }
        }
        return false;
    }

    public List<Exp> getSubExpressions() {
        List<Exp> list = new List<Exp>();
        Iterator<VarExp> paramsIt = params.iterator();
        while (paramsIt.hasNext()) {
            list.add((Exp) (paramsIt.next()));
        }
        return list;
    }

    public void setSubExpression(int index, Exp e) {
        params.set(index, (VarExp) e);
    }

    public void prettyPrint() {

        if (index != null) {
            System.out.print("(" + index.getName() + ") of ");
        }

        if (kind == AXIOM)
            System.out.print("Axiom ");
        else if (kind == THEOREM)
            System.out.print("Theorem ");
        else if (kind == PROPERTY)
            System.out.print("Property ");
        else if (kind == LEMMA)
            System.out.print("Lemma ");
        else if (kind == COROLLARY)
            System.out.print("Corollary ");
        else if (kind == SUPPOSITION)
            System.out.print("Supposition ");
        else if (kind == LINEREF)
            System.out.print("Line No. ");
        else if (kind == DEFINITION)
            System.out.print("Definition ");
        else if (kind == DEDUCTION)
            System.out.print("Deduction ");
        if (id != null)
            System.out.print(id.getName());
        if (params != null) {
            System.out.print("(");
            Iterator<VarExp> paramsIt = params.iterator();
            if (paramsIt.hasNext()) {
                paramsIt.next().prettyPrint();
            }
            while (paramsIt.hasNext()) {
                System.out.print(", ");
                paramsIt.next().prettyPrint();
            }
            System.out.print(")");
        }

        if (sourceModule != null) {
            System.out.print(" from " + sourceModule.getName());
        }
    }

    public Exp copy() {
        int newKind = kind;
        PosSymbol newId = null;
        if (id != null) {
            newId = id.copy();
        }

        PosSymbol newIndex = null;
        if (index != null) {
            newIndex = index.copy();
        }

        PosSymbol newSourceModule = null;
        if (sourceModule != null) {
            newSourceModule = sourceModule.copy();
        }

        List<VarExp> newParams = new List<VarExp>();
        Iterator<VarExp> paramsIt = params.iterator();
        while (paramsIt.hasNext()) {
            newParams.add((VarExp) (Exp.copy(paramsIt.next())));
        }
        return new MathRefExp(null, newKind, newId, newIndex, newSourceModule, newParams);
    }

}