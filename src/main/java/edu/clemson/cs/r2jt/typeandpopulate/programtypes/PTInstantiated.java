package edu.clemson.cs.r2jt.typeandpopulate.programtypes;

import edu.clemson.cs.r2jt.typeandpopulate.MTType;
import edu.clemson.cs.r2jt.typeandpopulate.entry.FacilityEntry;
import java.util.Map;

import edu.clemson.cs.r2jt.typereasoning.TypeGraph;

/**
 * <p>A <code>PTInstantiated</code> represents a <code>PTFamily</code> that has
 * been instantiated via a facility.</p>
 * 
 * <p>Note that, while an instantiated type must have all parameters "filled 
 * in", it's possible that some have been filled in with constant parameters
 * or type parameters from the facility's source module.</p>
 */
public class PTInstantiated extends PTType {

    /**
     * <p>A pointer to the entry in the symbol table corresponding to the 
     * facility that instantiated this type.</p>
     */
    private final FacilityEntry mySourceFacility;

    /**
     * <p>The name of the original type family.</p>
     */
    private final String myName;

    /**
     * <p>The mathematical model corresponding to this instantiated program
     * type.</p>
     */
    private final MTType myModel;

    public PTInstantiated(TypeGraph g, FacilityEntry facility, String familyName, MTType model) {
        super(g);

        mySourceFacility = facility;
        myModel = model;
        myName = familyName;
    }

    public FacilityEntry getInstantiatingFacility() {
        return mySourceFacility;
    }

    public String getFamilyName() {
        return myName;
    }

    @Override
    public MTType toMath() {
        return myModel;
    }

    @Override
    public PTType instantiateGenerics(Map<String, PTType> genericInstantiations,
            FacilityEntry instantiatingFacility) {

        //I'm already instantiated!
        return this;
    }

    @Override
    public boolean equals(Object o) {

        boolean result = (o instanceof PTInstantiated);

        if (result) {
            PTInstantiated oAsPTInstantiated = (PTInstantiated) o;

            result =
                    (mySourceFacility.equals(oAsPTInstantiated.getInstantiatingFacility()))
                            && myName.equals(oAsPTInstantiated.getFamilyName());
        }

        return result;
    }
}
