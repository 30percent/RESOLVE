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
 * FunctionExp.java
 * 
 * The Resolve Software Composition Workbench Project
 * 
 * Copyright (c) 1999-2005
 * Reusable Software Research Group
 * Department of Computer Science
 * Clemson University
 */

package edu.clemson.cs.r2jt.absyn;

import java.util.ListIterator;

import edu.clemson.cs.r2jt.collections.List;
import edu.clemson.cs.r2jt.data.Location;
import edu.clemson.cs.r2jt.data.PosSymbol;
import edu.clemson.cs.r2jt.data.Symbol;

import edu.clemson.cs.r2jt.typeandpopulate.entry.SymbolTableEntry;
import edu.clemson.cs.r2jt.proving.ChainingIterator;
import edu.clemson.cs.r2jt.proving.DummyIterator;

import edu.clemson.cs.r2jt.type.Type;
import edu.clemson.cs.r2jt.analysis.TypeResolutionException;
import edu.clemson.cs.r2jt.collections.Iterator;

public class FunctionExp extends AbstractFunctionExp {

    // ===========================================================
    // Variables
    // ===========================================================

    /** The location member. */
    private Location location;

    /** The qualifier member. */
    private PosSymbol qualifier;

    /** The name member. */
    private PosSymbol name;

    /** The natural member. */
    private Exp natural;

    /** The paramList member. */
    private List<FunctionArgList> paramList;

    /** If the type can be determined in the builder we set it here.  */
    private Type bType = null;

    //private boolean isab;

    private int quantification = VarExp.NONE;

    // ===========================================================
    // Constructors
    // ===========================================================

    public FunctionExp() {};

    public FunctionExp(Location location, PosSymbol qualifier, PosSymbol name, Exp natural,
            List<FunctionArgList> paramList) {
        this.location = location;
        this.qualifier = qualifier;
        this.name = name;
        this.natural = natural;
        this.paramList = paramList;
        //this.isab = false;
    }

    public FunctionExp(Location location, PosSymbol qualifier, PosSymbol name, Exp natural,
            List<FunctionArgList> paramList, Type b) {
        this.location = location;
        this.qualifier = qualifier;
        this.name = name;
        this.natural = natural;
        this.paramList = paramList;
        this.bType = b;
        //this.isab = false;
    }

    public FunctionExp(Location location, PosSymbol qualifier, PosSymbol name, Exp natural,
            List<FunctionArgList> paramList, int quantification) {
        this.location = location;
        this.qualifier = qualifier;
        this.name = name;
        this.natural = natural;
        this.paramList = paramList;
        //this.isab = false;
        this.quantification = quantification;
    }

    public boolean equivalent(Exp e) {
        boolean retval = e instanceof FunctionExp;

        if (retval) {
            FunctionExp eAsFunction = (FunctionExp) e;
            retval =
                    posSymbolEquivalent(qualifier, eAsFunction.qualifier)
                            && posSymbolEquivalent(name, eAsFunction.name)
                            && equivalent(natural, eAsFunction.natural)
                            && paramsEquivalent(paramList, eAsFunction.paramList)
                            && quantification == eAsFunction.quantification;
        }

        return retval;
    }

    private boolean paramsEquivalent(List<FunctionArgList> p1, List<FunctionArgList> p2) {
        boolean retval = true;

        Iterator<FunctionArgList> params1 = p1.iterator();
        Iterator<FunctionArgList> params2 = p2.iterator();
        FunctionArgList curP1, curP2;
        while (retval && params1.hasNext() && params2.hasNext()) {
            curP1 = params1.next();
            curP2 = params2.next();

            retval = functionArgListEquivalent(curP1, curP2);
        }

        return retval && !(params2.hasNext() || params1.hasNext());
    }

    private boolean functionArgListEquivalent(FunctionArgList l1, FunctionArgList l2) {
        boolean retval = true;

        Iterator<Exp> args1 = l1.getArguments().iterator();
        Iterator<Exp> args2 = l2.getArguments().iterator();
        while (retval && args1.hasNext() && args2.hasNext()) {
            retval = args1.next().equivalent(args2.next());
        }

        return retval && !(args2.hasNext() || args1.hasNext());
    }

    // ===========================================================
    // Accessor Methods
    // ===========================================================

    // -----------------------------------------------------------
    // Get Methods
    // -----------------------------------------------------------

    public int getQuantification() {
        return quantification;
    }

    /** Returns the value of the location variable. */
    @Override
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

    /** Returns the value of the natural variable. */
    public Exp getNatural() {
        return natural;
    }

    /** Returns the value of the paramList variable. */
    public List<FunctionArgList> getParamList() {
        return paramList;
    }

    /**
     * <p>Returns an <code>Iterator</code> over the arguments to this function.
     * call.</p>
     * 
     * @return An <code>Iterator</code> over the arguments to this function 
     *         call.
     */
    public java.util.Iterator<Exp> argumentIterator() {
        java.util.Iterator<Exp> retval = DummyIterator.getInstance((Iterator<Exp>) null);

        for (FunctionArgList l : paramList) {
            retval = new ChainingIterator<Exp>(l.getArguments().iterator(), retval);
        }

        return retval;
    }

    public Type getBtype() {
        return bType;
    }

    // -----------------------------------------------------------
    // Set Methods
    // -----------------------------------------------------------

    public void setQuantification(int quantification) {
        this.quantification = quantification;
    }

    @Override
    public void setQuantification(SymbolTableEntry.Quantification quantification) {
        this.quantification = quantification.toVarExpQuantificationCode();
    }

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

    /** Sets the natural variable to the specified value. */
    public void setNatural(Exp natural) {
        this.natural = natural;
    }

    /** Sets the paramList variable to the specified value. */
    public void setParamList(List<FunctionArgList> paramList) {
        this.paramList = paramList;
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    /**
     * <p>In addition to its normal substitution duties, for 
     * <code>FunctionExp</code> this method will also replace the function name
     * if it matches a <code>VarExp</code> in the map.</p>
     */
    public Exp substituteChildren(java.util.Map<Exp, Exp> substitutions) {
        FunctionExp retval;

        List<FunctionArgList> newParamList = new List<FunctionArgList>();
        List<Exp> oldArguments;
        List<Exp> newArguments;
        for (FunctionArgList l : paramList) {
            oldArguments = l.getArguments();
            newArguments = new List<Exp>();
            for (Exp a : oldArguments) {
                newArguments.add(substitute(a, substitutions));
            }
            newParamList.add(new FunctionArgList(newArguments));
        }

        VarExp newName = new VarExp(location, qualifier, name, quantification);

        if (substitutions.containsKey(newName)) {
            //Note that there's no particular mathematical justification why
            //we can only replace a function with a different function NAME (as
            //opposed to a function-valued expression), but we have no way of
            //representing such a thing.  It doesn't tend to come up, but if it
            //ever did, this would throw a ClassCastException.
            newName =
                    new VarExp(location, qualifier, ((VarExp) substitutions.get(newName)).getName(),
                            quantification);
        }

        retval =
                new FunctionExp(location, qualifier, newName.getName(), substitute(natural, substitutions),
                        newParamList, quantification);

        retval.setType(type);
        retval.setMathType(myMathType);

        return retval;
    }

    /** Accepts a ResolveConceptualVisitor. */
    public void accept(ResolveConceptualVisitor v) {
        v.visitFunctionExp(this);
    }

    /** Accepts a TypeResolutionVisitor. */
    public Type accept(TypeResolutionVisitor v) throws TypeResolutionException {
        return v.getFunctionExpType(this);
    }

    /** Returns a formatted text string of this class. */
    public String asString(int indent, int increment) {

        StringBuffer sb = new StringBuffer();

        printSpace(indent, sb);
        sb.append("FunctionExp\n");

        if (qualifier != null) {
            sb.append(qualifier.asString(indent + increment, increment));
        }

        if (name != null) {
            sb.append(name.asString(indent + increment, increment));
        }

        if (natural != null) {
            sb.append(natural.asString(indent + increment, increment));
        }

        if (paramList != null) {
            sb.append(paramList.asString(indent + increment, increment));
        }

        return sb.toString();
    }

    public String toString(int indent) {
        //Environment   env	= Environment.getInstance();
        //if(env.isabelle()){return toIsabelleString(indent);};    	
        StringBuffer sb = new StringBuffer();
        printSpace(indent, sb);

        if (name != null) {
            String strName = name.getName();
            int index = 0;
            int num = 0;
            while ((strName.charAt(index)) == '?') {
                num++;
                index++;
            }
            if (strName.substring(num).startsWith("Conc_")) {
                strName = strName.replace("Conc_", "Conc.");
            }
            sb.append(strName.substring(index, strName.length()));
            for (int i = 0; i < num; i++) {
                sb.append("'");
            }
        }
        sb.append("(");
        sb.append(paramListToString(paramList));
        sb.append(")");
        return sb.toString();
    }

    /*
    public String toIsabelleString(int indent){
    	isab = true;
    	StringBuffer sb = new StringBuffer();
    	printSpace(indent, sb);
        sb.append("(");
        sb.append(name.toString()+ " ");    

        sb.append(isabParamListToString(paramList));
        sb.append(")");
        return sb.toString(); 
    }*/

    String paramListToString(List<FunctionArgList> paramList) {
        if (paramList != null) {
            String str = new String();
            Iterator<FunctionArgList> i = paramList.iterator();
            while (i.hasNext()) {
                str = functionArgListToString((FunctionArgList) i.next());
            }
            return str;
        }
        else
            return new String();
    }

    String functionArgListToString(FunctionArgList list) {
        List<Exp> expList = list.getArguments();
        return expListToString(expList);
    }

    String expListToString(List<Exp> list) {
        StringBuffer str = new StringBuffer();
        Iterator<Exp> i = list.iterator();
        if (i.hasNext()) {
            Exp exp = (Exp) i.next();
            if (exp != null)
                str = str.append(exp.toString(0));
        }
        while (i.hasNext()) {
            Exp exp = (Exp) i.next();
            if (exp != null)
                str = str.append(", " + exp.toString(0));
        }
        return str.toString();
    }

    String isabParamListToString(List<FunctionArgList> paramList) {
        if (paramList != null) {
            String str = new String();
            Iterator<FunctionArgList> i = paramList.iterator();
            while (i.hasNext()) {
                str = isabFunctionArgListToString((FunctionArgList) i.next());
            }
            return str;
        }
        else
            return new String();
    }

    String isabFunctionArgListToString(FunctionArgList list) {
        List<Exp> expList = list.getArguments();
        return isabExpListToString(expList);
    }

    String isabExpListToString(List<Exp> list) {
        StringBuffer str = new StringBuffer();
        Iterator<Exp> i = list.iterator();
        if (i.hasNext()) {
            Exp exp = (Exp) i.next();
            if (exp != null)
                str = str.append(exp.toString(0));
        }
        while (i.hasNext()) {
            Exp exp = (Exp) i.next();
            if (exp != null)
                str = str.append(" " + exp.toString(0));
        }
        return str.toString();
    }

    /** Returns true if the variable is found in any sub expression   
        of this one. **/
    public boolean containsVar(String varName, boolean IsOldExp) {
        if (name.toString().equals(varName)) {
            return true;
        }
        if (natural != null) {
            return natural.containsVar(varName, IsOldExp);
        }
        if (paramList != null) {
            Iterator<FunctionArgList> i = paramList.iterator();
            while (i.hasNext()) {
                return funcArgListContainsVar(varName, i.next(), IsOldExp);
            }
        }
        return false;
    }

    private boolean funcArgListContainsVar(String varName, FunctionArgList list, Boolean IsOldExp) {
        List<Exp> expList = list.getArguments();
        Iterator<Exp> i = expList.iterator();
        while (i.hasNext()) {
            if (i.next().containsVar(varName, false))
                return true;
        }
        return false;

    }

    public Object clone() {
        FunctionExp clone = new FunctionExp();
        clone.quantification = quantification;
        clone.setLocation(this.getLocation());
        clone.setName(createPosSymbol(this.getName().toString()));
        clone.setQualifier(this.getQualifier());
        if (this.getNatural() != null)
            clone.setNatural((Exp) Exp.clone(this.getNatural()));
        if (paramList != null) {
            Iterator<FunctionArgList> i = paramList.iterator();
            List<FunctionArgList> newFAL = new List<FunctionArgList>();
            while (i.hasNext()) {
                newFAL.add((FunctionArgList) i.next().clone());
            }
            clone.setParamList(newFAL);
        }
        clone.setType(type);
        clone.setMathType(myMathType);
        return clone;
    }

    private PosSymbol createPosSymbol(String name) {
        PosSymbol posSym = new PosSymbol();
        posSym.setSymbol(Symbol.symbol(name));
        return posSym;
    }

    public List<Exp> getSubExpressions() {
        List<Exp> list = new List<Exp>();
        list.add(natural);
        list.addAll(paramList.get(0).getArguments());
        return list;
    }

    public void setSubExpression(int index, Exp e) {
        if (natural == null) {
            //List--for whatever reason--will not add null elements, so in
            //getSubExpression, the returned list will have "natural" (whatever
            //THAT means...) as its 0th element ONLY IF NATURAL IS NOT NULL, 
            //otherwise the parameters will start immediately
            paramList.get(0).getArguments().set(index, e);
        }
        else {
            switch (index) {
            case 0:
                natural = e;
                break;
            default:
                paramList.get(0).getArguments().set(index - 1, e);
                break;
            }
        }
    }

    public boolean shallowCompare(Exp e2) {
        if (!(e2 instanceof FunctionExp)) {
            return false;
        }
        if (qualifier != null && (((FunctionExp) e2).getQualifier() != null)) {
            if (!(qualifier.equals(((FunctionExp) e2).getQualifier().getName()))) {
                return false;
            }
        }
        if (!(name.equals(((FunctionExp) e2).getName().getName()))) {
            return false;
        }
        if (quantification != ((FunctionExp) e2).quantification) {
            return false;
        }

        return true;
    }

    public Exp replace(Exp old, Exp replacement) {
        if (!(old instanceof FunctionExp)) {
            paramList = replaceVariableInParamListWithExp(paramList, old, replacement);
            if (name != null) {
                if (old instanceof VarExp) {

                    if (((VarExp) old).getName().toString().equals(name.toString())) {
                        if (replacement instanceof VarExp) {

                            this.name = ((VarExp) replacement).getName();
                            this.qualifier = ((VarExp) replacement).getQualifier();
                            return this;
                        }
                        else if (replacement instanceof DotExp) {
                            DotExp exp = (DotExp) Exp.clone(replacement);
                            List<Exp> segments = exp.getSegments();
                            Exp result = Exp.replace(this, old, segments.get(segments.size() - 1));
                            segments.remove(segments.size() - 1);
                            segments.add(result);
                            exp.setSegments(segments);
                            return exp;
                        }
                    }
                }

                //if(old instanceof VarExp && replacement instanceof DotExp){
                //	if(((VarExp)old).getName().toString().equals(name.toString())){
                //		List<Exp> lst = ((DotExp)replacement).getSegments();
                //		this.name = ((VarExp)lst.get(1)).getName();
                //		this.qualifier = ((VarExp)lst.get(0)).getName();
                //		return this;
                //	}
                //}        		
            }
        }

        return this;
    }

    public List<FunctionArgList> replaceVariableInParamListWithExp(List<FunctionArgList> paramList, Exp old,
            Exp replacement) {
        if (paramList != null) {
            Iterator<FunctionArgList> i = paramList.iterator();
            while (i.hasNext()) {
                FunctionArgList tmp = i.next();
                i.previous();
                i.remove();
                i.add(replaceVariableInFunctionArgListWithExp((FunctionArgList) tmp, old, replacement));
            }
        }
        return paramList;
    }

    private FunctionArgList replaceVariableInFunctionArgListWithExp(FunctionArgList list, Exp old,
            Exp replacement) {
        List<Exp> expList = list.getArguments();
        list.setArguments(replaceVariableInExpListWithExp(expList, old, replacement));
        return list;
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

    public Exp remember() {

        rememberVariablesInParamList(this.getParamList());
        return this;
    }

    private void rememberVariablesInParamList(List<FunctionArgList> paramList) {
        Iterator<FunctionArgList> i = paramList.iterator();
        while (i.hasNext()) {
            rememberVariablesInFunctionArgList((FunctionArgList) i.next());
        }
    }

    private void rememberVariablesInFunctionArgList(FunctionArgList list) {
        List<Exp> expList = list.getArguments();
        rememberVariablesInExpList(expList);
    }

    private void rememberVariablesInExpList(List<Exp> list) {
        ListIterator<Exp> i = list.listIterator();
        while (i.hasNext()) {
            Exp exp = (Exp) i.next();
            if (exp instanceof OldExp) {
                exp = ((OldExp) exp).getExp();
                i.set(exp);
            }
            else
                exp = exp.remember();
        }
    }

    public void prettyPrint() {
        if (qualifier != null)
            System.out.print(qualifier.getName() + ".");
        System.out.print(name.getName() + "(");
        Iterator<Exp> it = paramList.get(0).getArguments().iterator();
        if (it.hasNext()) {
            it.next().prettyPrint();
        }
        while (it.hasNext()) {
            System.out.print(", ");
            it.next().prettyPrint();
        }
        System.out.print(")");
    }

    public Exp copy() {
        FunctionExp retval;

        PosSymbol newQualifier = null;
        if (qualifier != null)
            newQualifier = qualifier.copy();
        PosSymbol newName = name.copy();
        Exp newNatural = null;
        if (natural != null)
            newNatural = Exp.copy(natural);
        FunctionArgList newFAL = paramList.get(0).copy();
        List<FunctionArgList> newParamList = new List<FunctionArgList>();
        newParamList.add(newFAL);

        retval = new FunctionExp(null, newQualifier, newName, newNatural, newParamList, quantification);
        retval.setType(type);
        retval.setMathType(myMathType);
        return retval;
    }

    @Override
    public String getOperatorAsString() {
        return this.name.getName();
    }

    @Override
    public PosSymbol getOperatorAsPosSymbol() {
        return getName();
    }

}
