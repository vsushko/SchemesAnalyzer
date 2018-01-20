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
     * Возвращает описание схемы.
     */
    public static String getSchemaDescription(TreeNode<Object> node) {
        return (String) ((TreeNode) ((TreeNode) node.children.get(0).data).children.get(0)).data;
    }

    /**
     * Возвращащает список импортов.
     */
    public static List<String> getSchemaImports(TreeNode<Object> schemaTree) {
        List<String> importNames = new ArrayList<String>();

        List<TreeNode<Object>> schemaImports = ((TreeNode) schemaTree.children.get(2).data).children;

        for (TreeNode<Object> schemaImport : schemaImports) {
            if (schemaImport.data instanceof XmlSchemaImport) {
                importNames.add(((XmlSchemaImport) schemaImport.data).getNamespace());
            }
        }
        return importNames;
    }

    /**
     * Создает дерево схемы.
     */
    public static TreeNode<Object> buildSchemaTree(XmlSchema oldAfSchema, String schemaPath) {
        TreeBuilder treeBuilder = new TreeBuilderImpl();
        return treeBuilder.buildSchemaTree(oldAfSchema, schemaPath);
    }

    /**
     * Возвращает имена SimpleType элементов.
     */
    public static List<String> getSimpleTypesNamesFromTree(TreeNode<Object> schemaTree) {
        List<String> simpleTypesNames = new ArrayList<String>();

        List<TreeNode<Object>> simpleTypes = ((TreeNode) schemaTree.children.get(3).data).children;

        for (TreeNode<Object> simpleType : simpleTypes) {
            String typeName = ((XmlSchemaSimpleType)(simpleType.data)).getName();

            if (typeName != null) {
                simpleTypesNames.add(typeName);
            }
        }
        return simpleTypesNames;
    }

    /**
     * Возвращает список SimpleType с указанными именами.
     */
    public static List<XmlSchemaSimpleType> getSimpleTypesFromSchemaBySpecificNames(TreeNode<Object> oldSchemaTree,
                                                                                    List<String> specificNames) {
        List<XmlSchemaSimpleType> filteredSimpleTypes = new ArrayList<XmlSchemaSimpleType>();

        // достанем все SimpleType из дерева
        List<TreeNode<Object>> simpleTypes = ((TreeNode) oldSchemaTree.children.get(3).data).children;

        for (TreeNode<Object> simpleType : simpleTypes) {
            String typeName = ((XmlSchemaSimpleType) (simpleType.data)).getName();

            // добавляем только нужные SimpleType
            if (typeName != null && specificNames.contains(typeName)) {
                filteredSimpleTypes.add((XmlSchemaSimpleType) simpleType.data);
            }
        }
        return filteredSimpleTypes;
    }

    /**
     * Возвращает имена ComplexType элементов.
     */
    public static List<String> getComplexTypesNamesFromTree(TreeNode<Object> schemaTree) {
        List<String> complexTypeNames = new ArrayList<String>();

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
     * Возвращает список ComplexType с указанными именами.
     */
    public static List<XmlSchemaComplexType> getComplexTypesFromSchemaBySpecificNames(TreeNode<Object> oldSchemaTree,
                                                                                      List<String> specificNames) {
        List<XmlSchemaComplexType> filteredComplexType = new ArrayList<XmlSchemaComplexType>();

        // достанем все ComplexType из дерева
        List<TreeNode<Object>> complexTypes = ((TreeNode) oldSchemaTree.children.get(4).data).children;

        for (TreeNode<Object> complexType : complexTypes) {
            String typeName = ((XmlSchemaComplexType) (complexType.data)).getName();

            // добавляем только нужные ComplexType
            if (typeName != null && specificNames.contains(typeName)) {
                filteredComplexType.add((XmlSchemaComplexType) complexType.data);
            }
        }
        return filteredComplexType;
    }
}