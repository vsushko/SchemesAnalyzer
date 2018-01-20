package ru.vsushko.analyzer.schema.simpletype;

import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;

import java.util.List;

/**
 * Created by vsa
 * Date: 16.12.14.
 */
public interface Restriction {
    List<XmlSchemaFacet> getSchemaFacets(XmlSchemaSimpleTypeRestriction restriction);
}
