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
 * DefinitionDec.java
 * 
 * The Resolve Software Composition Workbench Project
 * 
 * Copyright (c) 1999-2005
 * Reusable Software Research Group
 * Department of Computer Science
 * Clemson University
 */

package edu.clemson.cs.r2jt.absyn;

import edu.clemson.cs.r2jt.collections.Iterator;
import edu.clemson.cs.r2jt.collections.List;
import edu.clemson.cs.r2jt.data.PosSymbol;

public class DefinitionDec extends Dec implements ModuleParameter {

    // ===========================================================
    // Variables
    // ===========================================================

    /** The implicit member. */
    private boolean implicit;

    /** The name member. */
    private PosSymbol name;

    /** The parameters member. */
    private List<MathVarDec> parameters;

    /** The returnTy member. */
    private Ty returnTy;

    private DefinitionBody body;

    // ===========================================================
    // Constructors
    // ===========================================================

    public DefinitionDec() {};

    public DefinitionDec(boolean implicit, PosSymbol name, List<MathVarDec> parameters, Ty returnTy,
            Exp base, Exp hypothesis, Exp definition) {
        this.implicit = implicit;
        this.name = name;
        this.parameters = parameters;
        this.returnTy = returnTy;
        if (!(base == null && hypothesis == null && definition == null)) {
            this.body = new DefinitionBody(base, hypothesis, definition);
        }
    }

    // ===========================================================
    // Accessor Methods
    // ===========================================================

    // -----------------------------------------------------------
    // Get Methods
    // -----------------------------------------------------------

    /** Returns the value of the implicit variable. */
    public boolean isImplicit() {
        return implicit;
    }

    public boolean isInductive() {
        return body == null ? false : body.isInductive();
    }

    /** Returns the value of the name variable. */
    public PosSymbol getName() {
        return name;
    }

    /** Returns the value of the parameters variable. */
    public List<MathVarDec> getParameters() {
        return parameters;
    }

    /** Returns the value of the returnTy variable. */
    public Ty getReturnTy() {
        return returnTy;
    }

    /** Returns the value of the base variable. */
    public Exp getBase() {
        return body == null ? null : body.getBase();
    }

    /** Returns the value of the hypothesis variable. */
    public Exp getHypothesis() {
        return body == null ? null : body.getHypothesis();
    }

    /** Returns the value of the definition variable. */
    public Exp getDefinition() {
        return body == null ? null : body.getDefinition();
    }

    // -----------------------------------------------------------
    // Set Methods
    // -----------------------------------------------------------

    /** Sets the implicit variable to the specified value. */
    public void setImplicit(boolean implicit) {
        this.implicit = implicit;
    }

    /** Sets the name variable to the specified value. */
    public void setName(PosSymbol name) {
        this.name = name;
    }

    /** Sets the parameters variable to the specified value. */
    public void setParameters(List<MathVarDec> parameters) {
        this.parameters = parameters;
    }

    /** Sets the returnTy variable to the specified value. */
    public void setReturnTy(Ty returnTy) {
        this.returnTy = returnTy;
    }

    /** Sets the base variable to the specified value. */
    public void setBase(Exp base) {
        body.setBase(base);
    }

    /** Sets the hypothesis variable to the specified value. */
    public void setHypothesis(Exp hypothesis) {
        body.setHypothesis(hypothesis);
    }

    /** Sets the definition variable to the specified value. */
    public void setDefinition(Exp definition) {
        body.setDefinition(definition);
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    /** Accepts a ResolveConceptualVisitor. */
    public void accept(ResolveConceptualVisitor v) {
        v.visitDefinitionDec(this);
    }

    /** Returns a formatted text string of this class. */
    public String asString(int indent, int increment) {

        StringBuffer sb = new StringBuffer();

        printSpace(indent, sb);
        sb.append("DefinitionDec\n");

        printSpace(indent + increment, sb);
        sb.append(implicit + "\n");

        if (name != null) {
            sb.append(name.asString(indent + increment, increment));
        }

        if (parameters != null) {
            sb.append(parameters.asString(indent + increment, increment));
        }

        if (returnTy != null) {
            sb.append(returnTy.asString(indent + increment, increment));
        }

        if (body != null) {
            sb.append(body.toString());
        }

        return sb.toString();
    }

    public void prettyPrint() {
        if (implicit) {
            System.out.print("Implicit ");
        }
        System.out.print("Definition " + name.getName());
        System.out.print("(");
        Iterator<MathVarDec> it = parameters.iterator();
        if (it.hasNext()) {
            it.next().prettyPrint();
        }
        System.out.print(") : ");
        returnTy.prettyPrint();
        if (implicit)
            System.out.print(" is ");
        else
            System.out.print(" = ");

        if (getBase() != null) {
            getBase().prettyPrint();
            System.out.println();
            getHypothesis().prettyPrint();
        }
        else {
            getDefinition().prettyPrint();
        }
    }

}
