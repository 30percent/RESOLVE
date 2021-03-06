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
 * BetweenExp.java
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
import edu.clemson.cs.r2jt.verification.AssertiveCode;
import edu.clemson.cs.r2jt.analysis.TypeResolutionException;
import edu.clemson.cs.r2jt.collections.Iterator;

public class BetweenExp extends Exp {

    // ===========================================================
    // Variables
    // ===========================================================

    /** The location member. */
    private Location location;

    /** The lessExps member. */
    private List<Exp> lessExps;

    // ===========================================================
    // Constructors
    // ===========================================================

    public BetweenExp() {
    // Empty
    }

    public BetweenExp(Location location, List<Exp> lessExps) {
        this.location = location;
        this.lessExps = lessExps;
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

    /** Returns the value of the lessExps variable. */
    public List<Exp> getLessExps() {
        return lessExps;
    }

    // -----------------------------------------------------------
    // Set Methods
    // -----------------------------------------------------------

    /** Sets the location variable to the specified value. */
    public void setLocation(Location location) {
        this.location = location;
    }

    /** Sets the lessExps variable to the specified value. */
    public void setLessExps(List<Exp> lessExps) {
        this.lessExps = lessExps;
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    public Exp substituteChildren(java.util.Map<Exp, Exp> substitutions) {
        List<Exp> newLessExps = new List<Exp>();
        for (Exp e : lessExps) {
            newLessExps.add(substitute(e, substitutions));
        }

        return new BetweenExp(location, newLessExps);
    }

    /** Accepts a ResolveConceptualVisitor. */
    public void accept(ResolveConceptualVisitor v) {
        v.visitBetweenExp(this);
    }

    /** Accepts a TypeResolutionVisitor. */
    public Type accept(TypeResolutionVisitor v) throws TypeResolutionException {
        return v.getBetweenExpType(this);
    }

    public Object clone() {
        BetweenExp clone = new BetweenExp();

        if (lessExps != null) {
            Iterator<Exp> i = lessExps.iterator();
            List<Exp> newLessExps = new List<Exp>();
            while (i.hasNext()) {
                newLessExps.add((Exp) Exp.clone(i.next()));
            }
            clone.setLessExps(newLessExps);
        }

        clone.setLocation(this.getLocation());
        clone.setType(type);
        return clone;
    }

    public Exp remember() {
        if (lessExps != null) {
            List<Exp> list = new List<Exp>();
            Iterator<Exp> i = this.lessExps.iterator();
            while (i.hasNext()) {
                list.add(i.next().remember());
            }
            this.lessExps = list;
        }

        return this;
    }

    /** Returns a formatted text string of this class. */
    public String asString(int indent, int increment) {

        StringBuffer sb = new StringBuffer();

        printSpace(indent, sb);
        sb.append("BetweenExp\n");

        if (lessExps != null) {
            sb.append(lessExps.asString(indent + increment, increment));
        }

        return sb.toString();
    }

    public String toString(int indent) {
        // 	Environment   env	= Environment.getInstance();
        // 	if(env.isabelle()){return toIsabelleString(indent);};    	
        StringBuffer sb = new StringBuffer();
        printSpace(indent, sb);
        List<Exp> list = lessExps;
        Iterator<Exp> i = list.iterator();
        while (i.hasNext()) {
            sb.append(i.next().toString(0));
            if (i.hasNext()) {
                sb.append(" and ");
            }
        }
        return sb.toString();
    }

    public String toIsabelleString(int indent) {
        StringBuffer sb = new StringBuffer();
        printSpace(indent, sb);
        List<Exp> list = lessExps;
        Iterator<Exp> i = list.iterator();
        while (i.hasNext()) {
            sb.append(i.next().toString(0));
            if (i.hasNext()) {
                if (!AssertiveCode.isProvePart())
                    sb.append(";\n");
                else
                    sb.append(" & ");
            }

        }
        return sb.toString();
    }

    /** Returns true if the variable is found in any sub expression   
        of this one. **/
    public boolean containsVar(String varName, boolean IsOldExp) {
        Iterator<Exp> i = lessExps.iterator();
        while (i.hasNext()) {
            Exp temp = i.next();
            if (temp != null) {
                if (temp.containsVar(varName, IsOldExp)) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<Exp> getSubExpressions() {
        return lessExps;
    }

    public void setSubExpression(int index, Exp e) {
        lessExps.set(index, e);
    }

    public boolean shallowCompare(Exp e2) {
        if (!(e2 instanceof BetweenExp)) {
            return false;
        }
        return true;
    }

    /**
     * <p>I don't really understand what a "BetweenExp" is, so for now its
     * 'equivalent' implementation just checks to see if all subexpressions
     * exist as a subexpression in <code>e</code>.  -HwS</p>
     */
    public boolean equivalent(Exp e) {
        boolean retval = (e instanceof BetweenExp);

        if (retval) {
            BetweenExp eAsBetweenExp = (BetweenExp) e;
            Iterator<Exp> eSubexpressions = eAsBetweenExp.getSubExpressions().iterator();
            Iterator<Exp> mySubexpressions;
            Exp curExp;
            while (retval && eSubexpressions.hasNext()) {
                curExp = eSubexpressions.next();
                mySubexpressions = lessExps.iterator();
                retval = false;
                while (!retval && mySubexpressions.hasNext()) {
                    retval = curExp.equivalent(mySubexpressions.next());
                }
            }
        }

        return retval;
    }

    public Exp replace(Exp old, Exp replace) {
        if (old instanceof BetweenExp) {
            return null;
        }
        else {
            lessExps = replaceVariableInExpListWithExp(this.lessExps, old, replace);
            return this;
        }
    }

    private List<Exp> replaceVariableInExpListWithExp(List<Exp> list, Exp old, Exp replacement) {
        // 	AssertiveCode assertion = new AssertiveCode();
        Iterator<Exp> i = list.iterator();
        while (i.hasNext()) {
            Exp exp = (Exp) i.next();
            Exp tmp = null;
            if (exp != null)
                tmp = Exp.replace(exp, old, replacement);
            i.previous();
            i.remove();
            if (tmp != null)
                i.add(tmp);
            else
                i.add(exp);
        }

        return list;
    }

    public void prettyPrint() {
        Iterator<Exp> it = lessExps.iterator();
        if (it.hasNext()) {
            it.next().prettyPrint();
        }
        while (it.hasNext()) {
            System.out.print(" and ");
            it.next().prettyPrint();
        }
    }

    public Exp copy() {
        Iterator<Exp> it = lessExps.iterator();
        List<Exp> newLessExps = new List<Exp>();
        while (it.hasNext()) {
            newLessExps.add(Exp.copy(it.next()));
        }
        Exp result = new BetweenExp(null, newLessExps);
        result.setType(type);
        return result;
    }

}
