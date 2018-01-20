package ru.vsushko.analyzer.schema.complextype.attribute;

import org.apache.ws.commons.schema.XmlSchemaAttribute;

/**
 * Created by vsa
 * Date: 11.12.14.
 */
public interface Attribute {

    /**
     * Возвращает значение fixed атрибута.
     */
    String getFixedValue(XmlSchemaAttribute schemaAttribute);

    /**
     * Возращает имя атрибута.
     */
    String getName(XmlSchemaAttribute schemaAttribute);

    /**
     * Возвращает тип атрибута без префикса.
     */
    String getType(XmlSchemaAttribute schemaAttribute);

    /**
     * Возвращает префикс атрибута.
     */
    String getPrefix(XmlSchemaAttribute schemaAttribute);

    /**
     * Возрвращает значение use атрибута
     */
    String getRequiredValue(XmlSchemaAttribute schemaAttribute);

    /**
     * Возвращает описание атрибута.
     */
    String getDescription(XmlSchemaAttribute schemaAttribute);

}
