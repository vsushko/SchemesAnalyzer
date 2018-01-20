package ru.vsushko.analyzer.schema;

import ru.vsushko.analyzer.schema.tree.TreeNode;

import java.util.List;

/**
 * Created by vsa
 * Date: 19.12.14.
 */
public interface DifferenceResolver {

    /**
     * Находит все изменения в схеме.
     */
    public void findAllDifference(TreeNode<Object> actualSchemaTree, TreeNode<Object> schemaToCompareTree);

    /**
     * Проверка на изменения в описании схемы.
     */
    public void checkDifferenceBetweenSchemaDescription(TreeNode<Object> actualSchemaTree, TreeNode<Object> schemaToCompareTree);

    /**
     * Проверка на изменения в импортах.
     */
    public void checkDifferenceBetweenSchemaImports(TreeNode<Object> actualSchemaTree, TreeNode<Object> schemaToCompareTree);

    /**
     * Проверка на изменения в SimpleType элементах.
     */
    public void checkDifferenceBetweenSimpleTypes(TreeNode<Object> actualSchemaTree, TreeNode<Object> schemaToCompareTree);

    /**
     * Проверка на изменения в ComplexType элементах.
     */
    public void checkDifferenceBetweenComplexTypes(TreeNode<Object> actualSchemaTree, TreeNode<Object> schemaToCompareTree);

    /**
     * Возвращает список всех изменений.
     */
    List<String> getDifferences();
}
