package edu.clemson.cs.r2jt.proving;

import edu.clemson.cs.r2jt.utilities.Mapping;

public class StaticProofDataSuggestionMapping implements Mapping<VCTransformer, ProofPathSuggestion> {

    private final ProofData myData;

    public StaticProofDataSuggestionMapping(ProofData data) {
        myData = data;
    }

    @Override
    public ProofPathSuggestion map(VCTransformer i) {
        return new ProofPathSuggestion(i, myData);
    }
}
