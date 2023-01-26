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
