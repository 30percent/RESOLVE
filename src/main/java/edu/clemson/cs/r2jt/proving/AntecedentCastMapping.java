package edu.clemson.cs.r2jt.proving;

import edu.clemson.cs.r2jt.utilities.Mapping;

public class AntecedentCastMapping implements Mapping<ImmutableConjuncts, Antecedent> {

    @Override
    public Antecedent map(ImmutableConjuncts i) {
        return new Antecedent(i);
    }
}
