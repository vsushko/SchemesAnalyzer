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
        // there is a bug.. if MaxOccurs has "unbounded"
        // value then we receive long value as a String
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
