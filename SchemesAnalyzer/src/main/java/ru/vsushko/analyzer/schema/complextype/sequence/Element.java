package ru.vsushko.analyzer.schema.complextype.sequence;

import org.apache.ws.commons.schema.XmlSchemaElement;

/**
 * Created by vsa
 * Date: 11.12.14.
 */
public interface Element {

    /**
     * Returns XmlSchemaElement name.
     */
    String getName(XmlSchemaElement schemaElement);

    /**
     * Returns XmlSchemaElement type.
     */
    String getType(XmlSchemaElement schemaElement);

    /**
     * Returns XmlSchemaElement prefix.
     */
    String getPrefix(XmlSchemaElement schemaElement);

    /**
     * Returns XmlSchemaElement MinOccur.
     */
    String getMinOccurs(XmlSchemaElement schemaElement);

    /**
     * Returns XmlSchemaElement MaxOccurs.
     */
    String getMaxOccurs(XmlSchemaElement schemaElement);

    /**
     * Returns XmlSchemaElement description.
     */
    String getDescription(XmlSchemaElement schemaElement);
}
