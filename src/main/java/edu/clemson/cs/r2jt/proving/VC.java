package edu.clemson.cs.r2jt.proving;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.clemson.cs.r2jt.proving.absyn.PExp;
import edu.clemson.cs.r2jt.proving.absyn.PSymbol;

/**
 * <p>Represents an immutable <em>verification condition</em>, which takes the 
 * form of a mathematical implication.</p>
 * 
 * <p>This class is intended to supersede and eventually replace
 * <code>VerificationCondition</code>.</p>
 */
public class VC {

    /**
     * <p>Name is a human-readable name for the VC used for debugging purposes.
     * </p>
     */
    private final String myName;

    /**
     * <p>myDerivedFlag is set to true to indicate that this VC is not the
     * original version of the VC with myName--rather it was derived from a
     * VC named myName (or derived from a VC derived from a VC named myName)</p>
     */
    private final boolean myDerivedFlag;

    private final Antecedent myAntecedent;
    private final Consequent myConsequent;

    public VC(String name, Antecedent antecedent, Consequent consequent) {
        this(name, antecedent, consequent, false);
    }

    public VC(String name, Antecedent antecedent, Consequent consequent, boolean derived) {

        myName = name;
        myAntecedent = antecedent;
        myConsequent = consequent;
        myDerivedFlag = derived;
    }

    public String getName() {
        String retval = myName;

        if (myDerivedFlag) {
            retval += " (modified)";
        }

        return retval;
    }

    public String getSourceName() {
        return myName;
    }

    public Antecedent getAntecedent() {
        return myAntecedent;
    }

    public Consequent getConsequent() {
        return myConsequent;
    }

    public VC substitute(Map<PExp, PExp> e) {
        return new VC(myName, myAntecedent.substitute(e), myConsequent.substitute(e), true);
    }

    public VC applyAntecedentExpansions() {

        VC retval;

        try {
            retval = applySingleExpansion(this).applyAntecedentExpansions();
        }
        catch (NoSuchReplacementException e) {
            retval = this;
        }

        return retval;
    }

    private static VC applySingleExpansion(VC original) throws NoSuchReplacementException {

        VC retval;

        Antecedent a = original.getAntecedent();
        Consequent c = original.getConsequent();

        int aSize = a.size();
        int conjunctIndex = 0;
        Map<PExp, PExp> replacement = new HashMap<PExp, PExp>();
        PExp conjunct;
        while (conjunctIndex < aSize && replacement.size() == 0) {
            conjunct = a.get(conjunctIndex);
            addIfExpansion(replacement, conjunct);
            conjunctIndex++;
        }

        if (replacement.size() > 0) {
            Antecedent newAntecedent = a.removed(conjunctIndex - 1).substitute(replacement);

            Consequent newConsequent = c.substitute(replacement);
            retval = new VC(original.getSourceName(), newAntecedent, newConsequent);
        }
        else {
            throw new NoSuchReplacementException();
        }

        return retval;
    }

    private static boolean addIfExpansion(Map<PExp, PExp> m, PExp e) {
        boolean retval = false;

        if (e.isEquality()) {
            PSymbol eAsPSymbol = (PSymbol) e;

            PExp left = eAsPSymbol.arguments.get(0);
            PExp right = eAsPSymbol.arguments.get(1);

            if (!right.isLiteral() && right.isVariable()) {
                List<PExp> arguments = new LinkedList<PExp>();
                arguments.add(right);
                arguments.add(left);

                eAsPSymbol = eAsPSymbol.setArguments(arguments);

                PExp swap = left;
                left = right;
                right = swap;
            }

            if (!left.isLiteral() && left.isVariable()) {

                m.put(left, right);
                retval = true;
            }
        }

        return retval;
    }

    public VC eliminateObviousConjuncts() {
        return new VC(myName, myAntecedent.eliminateObviousConjuncts(), myConsequent
                .eliminateObviousConjuncts(), true);
    }

    public VC eliminateRedundantConjuncts() {
        return new VC(myName, myAntecedent.eliminateRedundantConjuncts(), myConsequent
                .eliminateRedundantConjuncts(), true);
    }

    public VC simplify() {
        VC workingVC =
                this.applyAntecedentExpansions().eliminateObviousConjuncts().eliminateRedundantConjuncts();

        List<PExp> finalConsequents = new LinkedList<PExp>();
        Consequent newConsequent = workingVC.getConsequent();
        for (PExp e : newConsequent) {
            if (!myAntecedent.containsEqual(e)) {
                finalConsequents.add(e);
            }
        }

        return new VC(myName, workingVC.getAntecedent(), new Consequent(finalConsequents), true);
    }

    public VC applyToAntecedent(VC implication) {
        return new VC(myName, myAntecedent.apply(implication.getAntecedent(), implication.getConsequent()),
                myConsequent, true);
    }

    /**
     * <p>Returns true if this VC and the provided one are equivalent in terms
     * of structure, variable names, and function names.</p>
     * 
     * @param o The VC to compare this one to.
     * 
     * @return <code>True</code> <strong>iff</strong> <code>this</code> is
     *         equivalent to <code>o</code>.
     */
    public boolean equivalent(VC o) {
        return myAntecedent.equals(o.getAntecedent()) && myConsequent.equals(o.getConsequent());
    }

    public Set<String> getSymbolNames() {
        Set<String> retval = myAntecedent.getSymbolNames();
        retval.addAll(myAntecedent.getSymbolNames());

        return retval;
    }

    public String toString() {

        String retval = "========== " + getName() + " ==========\n" + myAntecedent + "  -->\n" + myConsequent;

        return retval;
    }
}
