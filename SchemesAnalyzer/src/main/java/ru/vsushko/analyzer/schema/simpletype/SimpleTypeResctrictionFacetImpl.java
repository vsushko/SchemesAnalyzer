package ru.vsushko.analyzer.schema.simpletype;

import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.apache.ws.commons.schema.XmlSchemaWhiteSpaceFacet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vsa
 * Date: 23.12.14.
 */
public class SimpleTypeResctrictionFacetImpl<T> implements SimpleTypeResctrictionFacet<T> {

    @Override
    public List<String> getDifferenceBetweenConcreteFacet(List<XmlSchemaFacet> oldFacets, List<XmlSchemaFacet> newFacets, T value, String label) {
        List<String> diffs = new ArrayList<>();

        T oldFacet = null;
        for (XmlSchemaFacet facet : oldFacets) {
            if (facet instanceof XmlSchemaWhiteSpaceFacet) {
                oldFacet = (T) facet;
            }
        }

        T newFacet = null;
        for (XmlSchemaFacet facet : newFacets) {
            if (facet instanceof XmlSchemaWhiteSpaceFacet) {
                newFacet = (T) facet;
            }
        }

        // added new restriction
        if (oldFacet == null && newFacet != null) {
            // diffs.add("Added new restriction " + label + " with value: " + newFacet.getValue());
        }

        // removed restriction which existed earlier
        if (newFacet == null && oldFacet != null) {
            // diffs.add("Removed restriction which existed earlier " + label + " with value: " + oldFacet.getValue());
        }

        // changed value
        /* if (oldFacet != null && newFacet != null
                && !oldFacet.getValue().equals(newFacet.getValue())) {
            diffs.add("Changed value " + label + " с " + oldFacet.getValue() + " на " + newFacet.getValue());
        } */
        return diffs;
    }
}
