package ru.vsushko.analyzer.schema.tree;

import org.apache.ws.commons.schema.*;

/**
 * Created by vsa
 * Date: 17.12.14.
 */
public interface TreeBuilder {

    /**
     * Строит дерево описания схемы.
     */
    TreeNode<Object> createSchemaDescriptionTree(String schemaFullPath);

    /**
     * Строит дерево элементов схемы.
     */
    TreeNode<Object> createSchemaElementsTree(XmlSchema xmlSchema);

    /**
     * Строит дерево импортов схемы.
     */
    TreeNode<Object> createSchemaImportsTree(XmlSchema xmlSchema);

    /**
     * Строит полное дерево всех SimpleType в схемы.
     */
    TreeNode<Object> createSimpleTypeTree(XmlSchema xmlSchema);

    /**
     * Строит полное дерево всех ComplexType схемы.
     */
    TreeNode<Object> createComplexTypeTree(XmlSchema xmlSchema);

    /**
     * Строит полное дерево схемы.
     */
    TreeNode<Object> buildSchemaTree(XmlSchema xmlSchema, String schemaPath);

}
