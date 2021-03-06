package edu.clemson.cs.r2jt.proving;

import java.util.Iterator;

import edu.clemson.cs.r2jt.utilities.Mapping;

/**
 * <p>A <code>ProductiveStepChooser</code> wraps an existing 
 * <code>TransformationChooser</code> and transparently returns only its
 * productive transformations, as defined in the class comments of
 * {@link edu.clemson.cs.r2jt.proving.ProductiveStepsOnlyStep 
 *    ProductiveStepsOnlyStep}.</p>
 */
public class ProductiveStepChooser implements TransformationChooser {

    private static final ProductiveFilterMapping PRODUCTIVE_FILTER = new ProductiveFilterMapping();

    private final TransformationChooser myBaseChooser;

    public ProductiveStepChooser(TransformationChooser c) {

        myBaseChooser = c;
    }

    @Override
    public void preoptimizeForVC(VC vc) {
        myBaseChooser.preoptimizeForVC(vc);
    }

    @Override
    public Iterator<ProofPathSuggestion> suggestTransformations(VC vc, int curLength, Metrics metrics,
            ProofData d) {

        return new LazyMappingIterator<ProofPathSuggestion, ProofPathSuggestion>(myBaseChooser
                .suggestTransformations(vc, curLength, metrics, d), PRODUCTIVE_FILTER);
    }

    @Override
    public String toString() {
        return "ProductiveStep(Steps from " + myBaseChooser + ")";
    }

    private static class ProductiveFilterMapping implements Mapping<ProofPathSuggestion, ProofPathSuggestion> {

        @Override
        public ProofPathSuggestion map(ProofPathSuggestion i) {

            return new ProofPathSuggestion(new ProductiveStepsOnlyStep(i.step), i.data, i.pathNote,
                    i.debugNote);
        }
    }
}
