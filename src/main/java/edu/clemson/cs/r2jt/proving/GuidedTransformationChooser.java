package edu.clemson.cs.r2jt.proving;

import java.util.Iterator;

public class GuidedTransformationChooser extends AbstractTransformationChooser {

    public GuidedTransformationChooser(Iterable<VCTransformer> library) {
        super(library);
    }

    @Override
    public Iterator<ProofPathSuggestion> doSuggestTransformations(VC vc, int curLength, Metrics metrics,
            ProofData d, Iterable<VCTransformer> localTheorems) {

        return new GuidedListSelectIterator<ProofPathSuggestion>("Choose rule", vc.toString(),
                new LazyMappingIterator<VCTransformer, ProofPathSuggestion>(getTransformerLibrary()
                        .iterator(), new StaticProofDataSuggestionMapping(d)));
    }
}
