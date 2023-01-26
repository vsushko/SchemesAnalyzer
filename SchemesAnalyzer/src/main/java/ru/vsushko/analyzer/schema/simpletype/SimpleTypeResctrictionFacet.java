package ru.vsushko.analyzer.schema.simpletype;

import org.apache.ws.commons.schema.XmlSchemaFacet;

import java.util.List;

/**
 * Created by vsa
 * Date: 23.12.14.
 */
public interface SimpleTypeResctrictionFacet<T> {

    List<String> getDifferenceBetweenConcreteFacet(List<XmlSchemaFacet> oldFacets, List<XmlSchemaFacet> newFacets, T value, String label);
}
