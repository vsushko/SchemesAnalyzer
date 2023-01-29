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
package ru.vsushko.analyzer.schema;

import ru.vsushko.analyzer.schema.tree.TreeNode;

import java.util.List;

/**
 * Created by vsa
 * Date: 19.12.14.
 */
public interface DifferenceResolver {

    /**
     * All schemas checks.
     */
    void findAllDifference(TreeNode<Object> actualSchemaTree, TreeNode<Object> schemaToCompareTree);

    /**
     * Schema description difference check.
     */
    void checkDifferenceBetweenSchemaDescription(TreeNode<Object> actualSchemaTree, TreeNode<Object> schemaToCompareTree);

    /**
     * Schema imports changes check.
     */
    void checkDifferenceBetweenSchemaImports(TreeNode<Object> actualSchemaTree, TreeNode<Object> schemaToCompareTree);

    /**
     * Schema SimpleType elements changes check.
     */
    void checkDifferenceBetweenSimpleTypes(TreeNode<Object> actualSchemaTree, TreeNode<Object> schemaToCompareTree);

    /**
     * Check for new ComplexType types existence.
     */
    void checkDifferenceBetweenComplexTypes(TreeNode<Object> actualSchemaTree, TreeNode<Object> schemaToCompareTree);

    /**
     * Returns list of changes which were identified during schemas difference comparison.
     */
    List<String> getDifferences();
}
