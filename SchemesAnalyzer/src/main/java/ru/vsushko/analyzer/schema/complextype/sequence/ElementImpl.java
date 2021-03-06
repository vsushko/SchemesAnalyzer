package ru.vsushko.analyzer.schema.complextype.sequence;

import org.apache.ws.commons.schema.XmlSchemaDocumentation;
import org.apache.ws.commons.schema.XmlSchemaElement;

/**
 * Created by vsa
 * Date: 11.12.14.
 */
public class ElementImpl implements Element {

    @Override
    public String getName(XmlSchemaElement schemaElement) {
        return schemaElement.getName();
    }

    @Override
    public String getType(XmlSchemaElement schemaElement) {
        return schemaElement.getSchemaTypeName().getLocalPart();
    }

    @Override
    public String getPrefix(XmlSchemaElement schemaElement) {
        return schemaElement.getSchemaTypeName().getPrefix();
    }

    @Override
    public String getMinOccurs(XmlSchemaElement schemaElement) {
        return String.valueOf(schemaElement.getMinOccurs());
    }

    @Override
    public String getMaxOccurs(XmlSchemaElement schemaElement) {
        // чей баг?.. если у MaxOccurs имеет значение unbounded,
        // то получаем строку в виде long
        return String.valueOf(schemaElement.getMaxOccurs());
    }

    @Override
    public String getDescription(XmlSchemaElement schemaElement) {
        String description;
        try {
            description = ((XmlSchemaDocumentation) schemaElement.getAnnotation().getItems().get(0)).getMarkup().item(0).getNodeValue();
            return description;
        } catch (NullPointerException e) {
            return "";
        }
    }
}
