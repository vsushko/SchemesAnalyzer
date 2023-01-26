package ru.vsushko.analyzer.schema.tree;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vsa
 * Date: 22.12.14.
 */
public class TreeHelper {

    private TreeHelper() throws Exception {
        throw new Exception("Could not init!");
    }

    /**
     * Returns schema description.
     */
    public static String getSchemaDescription(TreeNode<Object> node) {
        return (String) ((TreeNode) ((TreeNode) node.children.get(0).data).children.get(0)).data;
    }

    /**
     * Returns imports list.
     */
    public static List<String> getSchemaImports(TreeNode<Object> schemaTree) {
        List<String> importNames = new ArrayList<>();

        List<TreeNode<Object>> schemaImports = ((TreeNode) schemaTree.children.get(2).data).children;

        for (TreeNode<Object> schemaImport : schemaImports) {
            if (schemaImport.data instanceof XmlSchemaImport) {
                importNames.add(((XmlSchemaImport) schemaImport.data).getNamespace());
            }
        }
        return importNames;
    }

    /**
     * Creates schema tree.
     */
    public static TreeNode<Object> buildSchemaTree(XmlSchema oldAfSchema, String schemaPath) {
        TreeBuilder treeBuilder = new TreeBuilderImpl();
        return treeBuilder.buildSchemaTree(oldAfSchema, schemaPath);
    }

    /**
     * Retuurns SimpleType elements names.
     */
    public static List<String> getSimpleTypesNamesFromTree(TreeNode<Object> schemaTree) {
        List<String> simpleTypesNames = new ArrayList<>();

        List<TreeNode<Object>> simpleTypes = ((TreeNode) schemaTree.children.get(3).data).children;

        for (TreeNode<Object> simpleType : simpleTypes) {
            String typeName = ((XmlSchemaSimpleType) (simpleType.data)).getName();

            if (typeName != null) {
                simpleTypesNames.add(typeName);
            }
        }
        return simpleTypesNames;
    }

    /**
     * Returns SimpleType list with specified names.
     */
    public static List<XmlSchemaSimpleType> getSimpleTypesFromSchemaBySpecificNames(TreeNode<Object> oldSchemaTree, List<String> specificNames) {
        List<XmlSchemaSimpleType> filteredSimpleTypes = new ArrayList<>();

        // take all SimpleType children from tree
        List<TreeNode<Object>> simpleTypes = ((TreeNode) oldSchemaTree.children.get(3).data).children;

        for (TreeNode<Object> simpleType : simpleTypes) {
            String typeName = ((XmlSchemaSimpleType) (simpleType.data)).getName();

            // add only necessary SimpleType data
            if (typeName != null && specificNames.contains(typeName)) {
                filteredSimpleTypes.add((XmlSchemaSimpleType) simpleType.data);
            }
        }
        return filteredSimpleTypes;
    }

    /**
     * Returns ComplexType elements names.
     */
    public static List<String> getComplexTypesNamesFromTree(TreeNode<Object> schemaTree) {
        List<String> complexTypeNames = new ArrayList<>();

        List<TreeNode<Object>> complexTypes = ((TreeNode) schemaTree.children.get(4).data).children;

        for (TreeNode<Object> complexType : complexTypes) {
            String typeName = ((XmlSchemaComplexType) (complexType.data)).getName();

            if (typeName != null) {
                complexTypeNames.add(typeName);
            }
        }
        return complexTypeNames;
    }

    /**
     * Returns ComplexType elements with specified names.
     */
    public static List<XmlSchemaComplexType> getComplexTypesFromSchemaBySpecificNames(TreeNode<Object> oldSchemaTree, List<String> specificNames) {
        List<XmlSchemaComplexType> filteredComplexType = new ArrayList<>();

        // take all ComplexType children from tree
        List<TreeNode<Object>> complexTypes = ((TreeNode) oldSchemaTree.children.get(4).data).children;

        for (TreeNode<Object> complexType : complexTypes) {
            String typeName = ((XmlSchemaComplexType) (complexType.data)).getName();

            // add only necessary ComplexTypes
            if (typeName != null && specificNames.contains(typeName)) {
                filteredComplexType.add((XmlSchemaComplexType) complexType.data);
            }
        }
        return filteredComplexType;
    }
}