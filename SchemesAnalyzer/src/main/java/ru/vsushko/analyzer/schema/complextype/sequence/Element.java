package ru.vsushko.analyzer.schema.complextype.sequence;

import org.apache.ws.commons.schema.XmlSchemaElement;

/**
 * Created by vsa
 * Date: 11.12.14.
 */
public interface Element {

    /**
     * Возвращает имя элемента.
     */
    String getName(XmlSchemaElement schemaElement);

    /**
     * Возвращает тип элемента.
     */
    String getType(XmlSchemaElement schemaElement);

    /**
     * Возвращает префикс элемента.
     */
    String getPrefix(XmlSchemaElement schemaElement);

    /**
     * Возвращает MinOccurs элемента.
     */
    String getMinOccurs(XmlSchemaElement schemaElement);

    /**
     * Возвращает MaxOccurs элемента.
     */
    String getMaxOccurs(XmlSchemaElement schemaElement);

    /**
     * Возвращает описание элемента.
     */
    String getDescription(XmlSchemaElement schemaElement);

}
