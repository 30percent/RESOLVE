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
 * VariableRecordExp.java
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
import edu.clemson.cs.r2jt.data.PosSymbol;
import edu.clemson.cs.r2jt.type.Type;
import edu.clemson.cs.r2jt.analysis.TypeResolutionException;
import edu.clemson.cs.r2jt.collections.Iterator;

public class VariableRecordExp extends VariableExp {

    // ===========================================================
    // Variables
    // ===========================================================

    /** The location member. */
    private Location location;

    /** The qualifier member. */
    private PosSymbol qualifier;

    /** The name member. */
    private PosSymbol name;

    /** The fields member. */
    private List<VariableExp> fields;

    // ===========================================================
    // Constructors
    // ===========================================================

    public VariableRecordExp() {};

    public VariableRecordExp(Location location, PosSymbol qualifier, PosSymbol name, List<VariableExp> fields) {
        this.location = location;
        this.qualifier = qualifier;
        this.name = name;
        this.fields = fields;
    }

    public Exp substituteChildren(java.util.Map<Exp, Exp> substitutions) {
        List<VariableExp> newFields = new List<VariableExp>();
        for (VariableExp v : fields) {
            newFields.add((VariableExp) substitute(v, substitutions));
        }

        return new VariableRecordExp(location, qualifier, name, newFields);
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

    /** Returns the value of the qualifier variable. */
    public PosSymbol getQualifier() {
        return qualifier;
    }

    /** Returns the value of the name variable. */
    public PosSymbol getName() {
        return name;
    }

    /** Returns the value of the fields variable. */
    public List<VariableExp> getFields() {
        return fields;
    }

    // -----------------------------------------------------------
    // Set Methods
    // -----------------------------------------------------------

    /** Sets the location variable to the specified value. */
    public void setLocation(Location location) {
        this.location = location;
    }

    /** Sets the qualifier variable to the specified value. */
    public void setQualifier(PosSymbol qualifier) {
        this.qualifier = qualifier;
    }

    /** Sets the name variable to the specified value. */
    public void setName(PosSymbol name) {
        this.name = name;
    }

    /** Sets the fields variable to the specified value. */
    public void setFields(List<VariableExp> fields) {
        this.fields = fields;
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    /** Accepts a ResolveConceptualVisitor. */
    public void accept(ResolveConceptualVisitor v) {
        v.visitVariableRecordExp(this);
    }

    /** Accepts a TypeResolutionVisitor. */
    public Type accept(TypeResolutionVisitor v) throws TypeResolutionException {
        return v.getVariableRecordExpType(this);
    }

    /** Returns a formatted text string of this class. */
    public String asString(int indent, int increment) {

        StringBuffer sb = new StringBuffer();

        printSpace(indent, sb);
        sb.append("VariableRecordExp\n");

        if (qualifier != null) {
            sb.append(qualifier.asString(indent + increment, increment));
        }

        if (name != null) {
            sb.append(name.asString(indent + increment, increment));
        }

        if (fields != null) {
            sb.append(fields.asString(indent + increment, increment));
        }

        return sb.toString();
    }

    /** Returns a formatted text string of this class. */
    public String toString(int indent) {

        StringBuffer sb = new StringBuffer();
        /*
         printSpace(indent, sb);
         sb.append("VariableRecordExp\n");

         if (qualifier != null) {
         sb.append(qualifier.asString(indent+increment,increment));
         }

         if (name != null) {
         sb.append(name.asString(indent+increment,increment));
         }

         if (fields != null) {
         sb.append(fields.asString(indent+increment,increment));
         }
         */
        return sb.toString();
    }

    /** Returns true if the variable is found in any sub expression
        of this one. **/
    public boolean containsVar(String varName, boolean IsOldExp) {
        Iterator<VariableExp> i = fields.iterator();
        while (i.hasNext()) {
            VariableExp temp = i.next();
            if (temp != null) {
                if (temp.containsVar(varName, IsOldExp)) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<Exp> getSubExpressions() {
        List<Exp> list = new List<Exp>();
        Iterator<VariableExp> fieldsIt = fields.iterator();
        while (fieldsIt.hasNext()) {
            list.add((Exp) (fieldsIt.next()));
        }
        return list;
    }

    public void setSubExpression(int index, Exp e) {
        fields.set(index, (VariableExp) e);
    }
}
