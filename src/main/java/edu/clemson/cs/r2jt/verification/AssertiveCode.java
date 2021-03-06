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
package edu.clemson.cs.r2jt.verification;

import edu.clemson.cs.r2jt.ResolveCompiler;
import edu.clemson.cs.r2jt.absyn.*;

import java.util.concurrent.atomic.AtomicInteger;
import edu.clemson.cs.r2jt.collections.*;
import edu.clemson.cs.r2jt.data.Location;
import edu.clemson.cs.r2jt.data.ModuleID;
import edu.clemson.cs.r2jt.data.PosSymbol;
import edu.clemson.cs.r2jt.errors.ErrorHandler;
import edu.clemson.cs.r2jt.init.CompileEnvironment;
import edu.clemson.cs.r2jt.proving.absyn.PExp;
import edu.clemson.cs.r2jt.type.*;

import java.util.Hashtable;

public class AssertiveCode implements Cloneable {

    private ModuleID mySourceModule;
    private ErrorHandler err;

    List<VerificationStatement> assertive_code = new List<VerificationStatement>();
    Exp confirm;
    List<ConcType> freeVars2 = new List<ConcType>();
    Iterator<VerificationStatement> iter = assertive_code.iterator();
    private CompileEnvironment env;
    int count = 0;
    static int section = 0;
    String name = "";
    private boolean finalAssertion;
    static private boolean provePart = false;

    static public List<ConcType> currVars;
    static public Hashtable<String, Boolean> currVarHash = new Hashtable<String, Boolean>();

    public AssertiveCode(CompileEnvironment env) {
        this.env = env;
        confirm = Exp.getTrueVarExp();
        this.err = env.getErrorHandler();
    }

    /**
     * <p>Creates a new <code>AssertiveCode</code> object which will represent
     * the given <code>sourceModule</code>.</p>
     * 
     * @param sourceModule The module this assertive code will represent.
     */
    public AssertiveCode(ModuleID sourceModule, ErrorHandler err) {
        confirm = Exp.getTrueVarExp();
        mySourceModule = sourceModule;
        this.err = err;
    }

    /**
     * <p>Allows the VC numbering to be reset in cases where the compiler is not
     * being repeatedly instantiated for each VC generation job--e.g., when the
     * compiler is part of a web service and remains instantiated for the length
     * of the servlet's lifetime.</p> 
     *
     */
    public static void resetVCNumbering() {
        section = 0;
    }

    /**
     * <p>Returns the <code>ModuleID</code> of the module that this assertive
     * code is meant to represent.</p>
     * 
     * @return The module this assertive code represents.
     */
    public ModuleID getSourceModule() {
        return mySourceModule;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object clone() {
        AssertiveCode clone = new AssertiveCode(env);
        clone.setFinalConfirm((Exp) Exp.clone(confirm));
        clone.setIter(iter);

        Iterator<ConcType> k = freeVars2.iterator();
        while (k.hasNext()) {
            clone.addFreeVar((ConcType) (k.next().clone()));
        }

        Iterator<VerificationStatement> j = assertive_code.iterator();
        while (j.hasNext()) {
            VerificationStatement tmp = j.next();
            if ((tmp.getType()) == VerificationStatement.ASSUME) {
                clone.addAssume(Exp.copy(((Exp) tmp.getAssertion())));
            }
            else if (tmp.getType() == VerificationStatement.CONFIRM) {
                clone.addConfirm(Exp.copy(((Exp) tmp.getAssertion())));
            }
            else if (tmp.getType() == VerificationStatement.CODE) {
                clone.addCode((Statement) ((Statement) tmp.getAssertion()).clone());
            }
            else if (tmp.getType() == VerificationStatement.VARIABLE) {
                clone.addVariableDec((VarDec) ((VarDec) tmp.getAssertion()).clone());
            }
            else if (tmp.getType() == VerificationStatement.REMEMBER) {
                clone.addRemember();
            }
            else if (tmp.getType() == VerificationStatement.CHANGE && tmp.getAssertion() instanceof List<?>) {
                clone.addChange((List<?>) (((List<?>) tmp.getAssertion()).clone()));
            }
        }

        return clone;
    }

    public VerificationStatement getLastAssertion() {
        if (!assertive_code.isEmpty())
            return assertive_code.remove(assertive_code.size() - 1);
        else
            return new VerificationStatement();
    }

    public void setAssertiveCode(List<VerificationStatement> assertive_code) {
        this.assertive_code = assertive_code;
    }

    public void setIter(Iterator<VerificationStatement> iter) {
        this.iter = iter;
    }

    public void setFreeVars2(List<ConcType> freeVars) {
        this.freeVars2 = freeVars;
    }

    public boolean hasAnotherAssertion() {
        return (!assertive_code.isEmpty());
    }

    public void addFreeVar(ConcType freeVar) {
        if (freeVar != null && getFreeVar(freeVar.getName()) == null)
            freeVars2.add(freeVar);
    }

    public int getSection() {
        return section;
    }

    public List<ConcType> getFreeVars2() {
        return freeVars2;
    }

    public ConcType getFreeVar(PosSymbol name) {
        Iterator<ConcType> k = freeVars2.iterator();
        while (k.hasNext()) {
            ConcType tmp = k.next();
            if (tmp.getName().toString().equals(name.toString())) {
                return tmp;
            }
        }
        return null;
    }

    static public ConcType getCurrVar(PosSymbol name) {
        Iterator<ConcType> k = currVars.iterator();
        while (k.hasNext()) {
            ConcType tmp = k.next();
            if (tmp.getName().toString().equals(name.toString())) {
                if (currVarHash.get(name.toString()) == null || currVarHash.get(name.toString()) == false) {
                    currVarHash.put(name.toString(), true);
                    return tmp;
                }

            }
        }
        return null;
    }

    public void addVariableDec(VarDec variables) {
        assertive_code.add(new VerificationStatement(VerificationStatement.VARIABLE, variables));

    }

    public void insertVariableDec(VarDec variables) {
        assertive_code.add(0, new VerificationStatement(VerificationStatement.VARIABLE, variables));

    }

    public VarDec getVariableDec(String name) {
        for (VerificationStatement stmt : assertive_code) {
            if (stmt.getType() == VerificationStatement.getVariableType()) {
                if (((VarDec) ((VerificationStatement) stmt).getAssertion()).getName().getName().equals(name)) {
                    return ((VarDec) ((VerificationStatement) stmt).getAssertion());
                }
            }
        }
        return null;

    }

    public void addChange(List<?> list) {
        assertive_code.add(new VerificationStatement(VerificationStatement.CHANGE, list));

    }

    public void addVariableDecs(List<VarDec> variables) {
        Iterator<VarDec> i = variables.iterator();
        while (i.hasNext()) {
            addVariableDec((VarDec) i.next().clone());
        }
    }

    public void addCode(Statement stmt) {
        assertive_code.add(new VerificationStatement(VerificationStatement.getCodeType(), stmt));
    }

    public void addStatements(List<Statement> statements) {
        Iterator<Statement> i = statements.iterator();
        while (i.hasNext()) {
            assertive_code.add(new VerificationStatement(VerificationStatement.getCodeType(), i.next()));
        }
    }

    public void addConfirm(Exp confirm) {
        if (confirm != null) {

            ConfirmStmt conf = new ConfirmStmt();
            conf.setAssertion(confirm);
            addCode(conf);
        }
    }

    public Exp getFinalConfirm() {
        return (Exp) Exp.clone(this.confirm);
    }

    public void setFinalConfirm(Exp confirm) {
        if (confirm == null) {
            confirm = VarExp.getTrueVarExp();
        }

        this.confirm = confirm;
    }

    public void addAssume(Exp assume) {
        if (assume != null) {
            AssumeStmt conf = new AssumeStmt();
            conf.setAssertion(assume);
            addCode(conf);
        }
    }

    public void addRemember() {
        assertive_code.add(new VerificationStatement(VerificationStatement.getRememberType()));
    }

    private String splitAssertionToString() {
        Iterator<ConcType> freeVar2Iter = freeVars2.iterator();
        Iterator<VerificationStatement> i = assertive_code.iterator();
        String str = new String();

        currVars = freeVars2;
        currVarHash.clear();

        // Check to see if we want to output an XML file
        Boolean XMLfile = env.flags.isFlagSet(ResolveCompiler.FLAG_XML_OUT);

        //if(env.isabelle() && false) str = str.concat("(*");
        //else { str = str.concat("\n");}
        str = str.concat("\n");
        if (!env.flags.isFlagSet(Verifier.FLAG_ISABELLE_VC)) {
            str = str.concat("Free Variables: \n");
            while (freeVar2Iter.hasNext()) {

                str = str.concat(concTypeToString(freeVar2Iter.next()));

                if (freeVar2Iter.hasNext())
                    str = str.concat(", ");
                else
                    str = str.concat("");
            }
            if (env.flags.isFlagSet(Verifier.FLAG_ISABELLE_VC))
                str = str.concat(" *)");
            str = str.concat("\n");

            // encode for web interface if XMLout flag is set
            if (env.flags.isFlagSet(ResolveCompiler.FLAG_XML_OUT)) {
                str = "{\"freeVars\":\"" + ResolveCompiler.webEncode(str) + "\"},";
            }
        }
        while (i.hasNext()) {
            VerificationStatement cur = i.next();
            if (cur.getType() == VerificationStatement.getAssumeType()) {
                str = str.concat("Assume " + expToString((Exp) cur.getAssertion()));
            }
            else if (cur.getType() == VerificationStatement.getConfirmType())
                str = str.concat("Confirm " + expToString((Exp) cur.getAssertion()));
            else if (cur.getType() == VerificationStatement.getCodeType())
                str = str.concat(stmtToString((Statement) cur.getAssertion()));
            else if (cur.getType() == VerificationStatement.getRememberType())
                str = str.concat("Remember");
            else if (cur.getType() == VerificationStatement.getVariableType())
                str = str.concat("\t" + varDecToString((VarDec) cur.getAssertion()));
            else if (cur.getType() == VerificationStatement.CHANGE) {

                str = str.concat("Change ");

                Iterator<ConcType> j =
                        (Iterator<ConcType>) (((List<ConcType>) cur.getAssertion()).iterator());

                while (j.hasNext()) {
                    str = str.concat(concTypeToString(j.next()));

                    if (j.hasNext())
                        str = str.concat(", ");
                    else
                        str = str.concat("");
                }
            }
            else {}
            str = str.concat(";\n");
        }

        //	str = str.concat("Confirm \n" + expToString((Exp)(confirm.clone())) + ";");
        String splEXP = splitExpToString((Exp) (Exp.clone(confirm)));
        str = str.concat("\n\n" + splEXP);
        return str;
    }

    public String assertionToString(boolean finalAssert) {

        this.finalAssertion = finalAssert;
        String output = assertionToString();
        this.finalAssertion = false;
        //	count = 0;
        if (finalAssert) {
            section++;
        }//count = ((count / 10) + 1) * 10;    	
        return output;
    }

    String assertionToString() {
        Iterator<ConcType> freeVar2Iter = freeVars2.iterator();
        Iterator<VerificationStatement> i = assertive_code.iterator();
        String str = new String();
        count = 0;

        if (env.flags.isFlagSet(Verifier.FLAG_VERIFY_VC))
            return splitAssertionToString();

        str = str.concat("\nFree Variables: \n");
        while (freeVar2Iter.hasNext()) {

            str = str.concat(concTypeToString(freeVar2Iter.next()));

            if (freeVar2Iter.hasNext())
                str = str.concat(", ");
            else
                str = str.concat("\n");
        }

        // encode for web interface if XMLout flag is set
        if (env.flags.isFlagSet(ResolveCompiler.FLAG_XML_OUT)) {
            str = ResolveCompiler.webEncode(str);
        }

        str = str.concat("\n");
        provePart = true;

        while (i.hasNext()) {
            VerificationStatement cur = i.next();
            if (cur.getType() == VerificationStatement.getAssumeType()) {
                str = str.concat("Assume " + expToString((Exp) cur.getAssertion()));
            }
            else if (cur.getType() == VerificationStatement.getConfirmType())
                str = str.concat("Confirm " + expToString((Exp) cur.getAssertion()));
            else if (cur.getType() == VerificationStatement.getCodeType())
                str = str.concat(stmtToString((Statement) cur.getAssertion()));
            else if (cur.getType() == VerificationStatement.getRememberType())
                str = str.concat("Remember");
            else if (cur.getType() == VerificationStatement.getVariableType())
                str = str.concat("\t" + varDecToString((VarDec) cur.getAssertion()));
            else if (cur.getType() == VerificationStatement.CHANGE) {

                str = str.concat("Change ");

                Iterator<ConcType> j = ((List) cur.getAssertion()).iterator();

                while (j.hasNext()) {
                    str = str.concat(concTypeToString(j.next()));

                    if (j.hasNext())
                        str = str.concat(", ");
                    else
                        str = str.concat("");
                }
            }
            else {}
            str = str.concat(";\n");
        }

        str = str.concat("Confirm \n" + expToString((Exp) confirm) + ";");
        provePart = false;

        return str;
    }

    private String stmtToString(Statement stmt) {
        return stmt.toString(6);
    }

    private String varDecToString(VarDec dec) {
        String str = new String("Var ");
        str = str.concat(dec.getName() + ":");

        if (dec.getTy() instanceof NameTy) {
            str = str.concat(((NameTy) dec.getTy()).getName().toString());
        }
        else if (dec.getTy() instanceof ArrayTy) {
            str = str.concat(((ArrayTy) dec.getTy()).toString());
        }

        return str;
    }

    private String concTypeToString(ConcType freeVar) {
        StringBuffer sb = new StringBuffer();
        VarExp exp = new VarExp(null, null, freeVar.getName());
        //if(env.flags.isFlagSet(Verifier.FLAG_ISABELLE_VC))
        //sb.append(exp.toIsabelleString(0));
        //else
        sb.append(exp.toString(0));
        if (!(freeVar.getType() instanceof VoidType))
            sb.append(":" + freeVar.getType().asString());
        return sb.toString();
    }

    String expToString(Exp exp) {
        if (env.flags.isFlagSet(Verifier.FLAG_ISABELLE_VC)) {
            return exp.toString(0);
        }
        return exp.toString(0);
    }

    String splitExpToString(Exp exp) {
        List<InfixExp> tmp = exp.split();

        Iterator<InfixExp> it2 = tmp.iterator();
        List<InfixExp> tmp2 = new List<InfixExp>();
        while (it2.hasNext()) {
            Exp tmpExp = it2.next();//.simplify();
            if (!tmpExp.equals(VarExp.getTrueVarExp()) && tmpExp instanceof InfixExp) {
                tmp2.add((InfixExp) tmpExp);
            }

        }

        Iterator<InfixExp> it = tmp2.iterator();
        StringBuffer sb = new StringBuffer();

        // temp buffer in case we need to encode output for XML
        StringBuffer tb = new StringBuffer();
        //tb.append("Confirm: " + exp.toString(0) + "\n");    
        // Check to see if we want to output an XML file
        Boolean XMLfile = env.flags.isFlagSet(ResolveCompiler.FLAG_XML_OUT);

        while (it.hasNext()) {
            currVarHash.clear();

            InfixExp tmpInf = (InfixExp) it.next();
            if (env.flags.isFlagSet(Verifier.FLAG_ISABELLE_VC)) {
                PrintAssertions printer = new PrintAssertions(env);
                updateTheCount();
                // This will be replaced by code below
                sb.append("lemma " + name + "_" + section + "_" + count + ":\n");
                if (tmpInf.getLeft() == null) {
                    sb.append("\"[| \n" + " \n|] ");

                }
                else {
                    sb.append("\"[| \n" + printer.clearAndVisitAssertion(tmpInf.getLeft()) + " \n|] ");
                }
                sb.append("\n==> \n");
                provePart = true;
                sb.append(printer.clearAndVisitAssertion(tmpInf.getRight()));

                provePart = false;
                sb.append("\"\n" +
                //	"apply (((simp only: simp_thms), clarify?)+)?;\n" +
                        "apply auto;\n" + "done\n\n");

                currVarHash.clear();

            }
            else {

                PrintAssertions printer = new PrintAssertions(env);

                updateTheCount();

                if (XMLfile) {
                    //sb.append("<vc id=\"" + section + "_" + count + "\">");
                    //sb.append("<vcNum>");
                    Location loc = null;
                    if (tmpInf instanceof InfixExp) {
                        if (((InfixExp) tmpInf).getRight().getLocation() != null) {
                            loc = ((InfixExp) tmpInf).getRight().getLocation();
                        }
                    }
                    else {
                        loc = tmpInf.getLocation();
                    }
                    sb.append("{\"vc\":\"" + section + "_" + count + "\",");
                    sb.append("\"sourceFile\":\"" + ((loc != null) ? loc.getFilename() : "") + "\",");
                    sb.append("\"lineNum\":\"" + ((loc != null) ? loc.getPos().getLine() : 0) + "\",");
                    sb.append("\"vcInfo\":\"");
                }

                /* Stuff to appear within the vc tag element */
                if (!XMLfile) {
                    tb.append("\nVC: " + section + "_" + count + ": \n");
                }
                tb.append(getLocationInformation(tmpInf) + "\n\n");
                /* end vc tag element */

                // If XML file is requested, we need to encode it to get rid
                // of any illegal XML chars inside (i.e. < and >)
                if (XMLfile) {
                    String s = ResolveCompiler.webEncode(tb.toString());
                    tb = new StringBuffer();
                    tb.append(s);
                }
                // add the stuff in the temp buffer to the real buffer and clear tb
                sb.append(tb.toString());
                tb = new StringBuffer();

                //if(XMLfile) sb.append("</vcNum>");
                //if(XMLfile) sb.append("<vcGoal>");
                if (XMLfile) {
                    sb.append("\",");
                    sb.append("\"vcGoal\":\"");
                }
                //tb.append("Confirm: " + tmpInf.toString(0) + "\n");
                /* Stuff inside the goal tag */
                tb.append("Goal:\n");
                provePart = true;
                tb.append(printer.clearAndVisitAssertion(tmpInf.getRight()));
                /* end goal tag */

                if (XMLfile) {
                    String s = ResolveCompiler.webEncode(tb.toString());
                    tb = new StringBuffer();
                    tb.append(s);
                }

                sb.append(tb.toString());
                tb = new StringBuffer();

                //if(XMLfile) sb.append("</vcGoal>");
                if (XMLfile)
                    sb.append("\",");
                provePart = false;
                //if(XMLfile) sb.append("<vcGiven>");
                if (XMLfile)
                    sb.append("\"vcGivens\":\"");

                /* given tag stuff */
                tb.append("\n\nGiven:\n");

                if (tmpInf.getLeft() == null) {

                }
                else if (tmpInf.getLeft() instanceof InfixExp
                        && ((InfixExp) tmpInf.getLeft()).getOpName().equals("and")) {
                    tb.append(printer.clearAndVisitAssertion(tmpInf.getLeft()));
                }
                else {
                    tb.append("\n1: " + printer.clearAndVisitAssertion(tmpInf.getLeft()));
                }
                /* end given stuff */

                if (XMLfile) {
                    String s = ResolveCompiler.webEncode(tb.toString());
                    tb = new StringBuffer();
                    tb.append(s);
                }

                sb.append(tb.toString());
                tb = new StringBuffer();

                //if(XMLfile) sb.append("</vcGiven>");  
                if (XMLfile)
                    sb.append("\"");

                if (finalAssertion && env.flags.isFlagSet(Verifier.FLAG_REASON_FOR_GIVEN)) {
                    //if(XMLfile) sb.append("<vcReasons>");
                    if (XMLfile)
                        sb.append(",\"vcReasons\":\"");

                    /* reasons tag stuff */

                    sb.append("\n\nReasons for Assumptions:");

                    if (tmpInf.getLeft() == null) {
                        tb.append("\nNo Assumptions for this VC");
                    }
                    else if (tmpInf.getLeft() instanceof InfixExp
                            && ((InfixExp) tmpInf.getLeft()).getOpName().equals("and")) {
                        tb.append("" + ((InfixExp) tmpInf.getLeft()).printLocation(new AtomicInteger(0)));
                    }
                    else if (tmpInf.getLeft().getLocation() != null) {
                        tb.append("\n1: " + tmpInf.getLeft().getLocation() + ": "
                                + tmpInf.getLeft().getLocation().getDetails());
                    }
                    /* end reasons stuff */

                    if (XMLfile) {
                        String s = ResolveCompiler.webEncode(tb.toString());
                        tb = new StringBuffer();
                        tb.append(s);
                    }

                    sb.append(tb.toString());
                    tb = new StringBuffer();

                    //if(XMLfile) sb.append("</vcReasons>");
                    if (XMLfile)
                        sb.append("\"");
                }
                //if(XMLfile) sb.append("</vc>");
                if (XMLfile) {
                    sb.append("},");
                }
                else {
                    sb.append("\n\n");
                }
            }

        }
        return sb.toString();
    }

    private void updateTheCount() {
        count++;
    }

    private String getLocationInformation(Exp exp) {
        Location loc = null;
        StringBuffer sb = new StringBuffer();
        if (exp instanceof InfixExp) {

            if (((InfixExp) exp).getRight().getLocation() != null) {
                loc = ((InfixExp) exp).getRight().getLocation();

            }

        }
        else {
            loc = exp.getLocation();
        }
        if (loc != null) {
            if (loc.getDetails() != null) {
                sb.append(loc.getDetails());
            }
            if (loc.getPos() != null) {
                sb.append(": " + loc.toString());
            }
        }
        return sb.toString();
    }

    static public boolean isProvePart() {
        return provePart;
    }

    @Override
    public String toString() {
        String result = "";

        for (VerificationStatement s : assertive_code) {
            result += s.allInfo() + "\n";
        }

        return result;
    }
}
