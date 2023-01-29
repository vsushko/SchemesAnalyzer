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
