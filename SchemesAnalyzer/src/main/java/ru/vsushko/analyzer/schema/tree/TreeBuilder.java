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
package ru.vsushko.analyzer.schema.tree;

import org.apache.ws.commons.schema.XmlSchema;

/**
 * Created by vsa
 * Date: 17.12.14.
 */
public interface TreeBuilder {

    /**
     * Creates schema description tree.
     */
    TreeNode<Object> createSchemaDescriptionTree(String schemaFullPath);

    /**
     * Creates schema elements tree.
     */
    TreeNode<Object> createSchemaElementsTree(XmlSchema xmlSchema);

    /**
     * Creates schema imports tree.
     */
    TreeNode<Object> createSchemaImportsTree(XmlSchema xmlSchema);

    /**
     * Turns out SimpleType into tree.
     */
    TreeNode<Object> createSimpleTypeTree(XmlSchema xmlSchema);

    /**
     * Turns out ComplexType into tree.
     */
    TreeNode<Object> createComplexTypeTree(XmlSchema xmlSchema);

    /**
     * Creates complete schema tree.
     */
    TreeNode<Object> buildSchemaTree(XmlSchema xmlSchema, String schemaPath);
}
