package ru.vsushko.analyzer.schema.complextype.attribute;

import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaDocumentation;

/**
 * Get values from Attribute.
 * <p>
 * Created by vsa
 * Date: 11.12.14.
 */
public class AttributeImpl implements Attribute {
    @Override
    public String getFixedValue(XmlSchemaAttribute schemaAttribute) {
        return schemaAttribute.getFixedValue();
    }

    @Override
    public String getName(XmlSchemaAttribute schemaAttribute) {
        return schemaAttribute.getName();
    }

    @Override
    public String getType(XmlSchemaAttribute schemaAttribute) {
        return schemaAttribute.getSchemaTypeName().getLocalPart();
    }

    @Override
    public String getPrefix(XmlSchemaAttribute schemaAttribute) {
        return schemaAttribute.getSchemaTypeName().getPrefix();
    }

    @Override
    public String getRequiredValue(XmlSchemaAttribute schemaAttribute) {
        return schemaAttribute.getUse().toString();
    }

    @Override
    public String getDescription(XmlSchemaAttribute schemaAttribute) {
        String description;
        try {
            description = ((XmlSchemaDocumentation) schemaAttribute.getAnnotation().getItems().get(0)).getMarkup().item(0).getNodeValue();
            return description;
        } catch (NullPointerException e) {
            return "";
        }
    }
}
