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
