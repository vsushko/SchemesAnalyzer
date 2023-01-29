/**
 * Copyright (C) SchemesAnalyzer.2014
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Vasiliy Sushko (vasiliy.sushko@gmail.com)
 */
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
