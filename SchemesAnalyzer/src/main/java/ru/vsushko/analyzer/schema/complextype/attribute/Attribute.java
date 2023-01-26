package ru.vsushko.analyzer.schema.complextype.attribute;

import org.apache.ws.commons.schema.XmlSchemaAttribute;

/**
 * Get values from Attribute.
 * <p>
 * Created by vsa
 * Date: 11.12.14.
 */
public interface Attribute {

    /**
     * Returns fixed attribute value.
     */
    String getFixedValue(XmlSchemaAttribute schemaAttribute);

    /**
     * Returns attribute name.
     */
    String getName(XmlSchemaAttribute schemaAttribute);

    /**
     * Returns attribute type without prefix.
     */
    String getType(XmlSchemaAttribute schemaAttribute);

    /**
     * Returns attribute prefix.
     */
    String getPrefix(XmlSchemaAttribute schemaAttribute);

    /**
     * Returns use attribute value.
     */
    String getRequiredValue(XmlSchemaAttribute schemaAttribute);

    /**
     * Returns attribute description.
     */
    String getDescription(XmlSchemaAttribute schemaAttribute);
}
