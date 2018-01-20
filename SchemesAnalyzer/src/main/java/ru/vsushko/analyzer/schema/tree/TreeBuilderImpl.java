package ru.vsushko.analyzer.schema.tree;

import org.apache.ws.commons.schema.*;
import ru.vsushko.analyzer.schema.SchemaHelper;

import java.util.List;

/**
 * Created by vsa
 * Date: 18.12.14.
 */
public class TreeBuilderImpl implements TreeBuilder {

    /**
     * Строит дерево описания схемы.
     */
    @Override
    public TreeNode<Object> createSchemaDescriptionTree(String schemaFullPath) {
        TreeNode<Object> schemaDescription = new TreeNode<Object>("SchemaName");
        schemaDescription.addChild(SchemaHelper.getSchemaDescription(schemaFullPath));
        return schemaDescription;
    }

    /**
     * Строит дерево элементов схемы.
     */
    @Override
    public TreeNode<Object> createSchemaElementsTree(XmlSchema xmlSchema) {
        TreeNode<Object> schemaElement = new TreeNode<Object>("Elements");
        schemaElement.addChild(SchemaHelper.getElementsMap(xmlSchema));
        return schemaElement;
    }

    /**
     * Строит дерево импортов схемы.
     */
    @Override
    public TreeNode<Object> createSchemaImportsTree(XmlSchema xmlSchema) {
        TreeNode<Object> schemaImports = new TreeNode<Object>("Imports");

        for (XmlSchemaImport schemaImport : SchemaHelper.getSchemaImports(xmlSchema)) {
            schemaImports.addChild(schemaImport);
        }
        return schemaImports;
    }

    /**
     * Раскладывает SimpleType в дерево.
     */
    @Override
    public TreeNode<Object> createSimpleTypeTree(XmlSchema xmlSchema) {
        TreeNode<Object> simpleTypesNode = new TreeNode<Object>("SimpleType");

        // получим все SimpleType
        for (XmlSchemaSimpleType simpleType : SchemaHelper.getSimpleTypeItems(xmlSchema)) {
            simpleTypesNode.addChild(simpleType);
        }

        // имеет ли смысл дальше работать
        if (simpleTypesNode.children.size() != 0) {
            for (TreeNode<Object> simpleTypeNode : simpleTypesNode.children) {

                // добавляем annotation
                XmlSchemaAnnotation annotation = ((XmlSchemaSimpleType) simpleTypeNode.data).getAnnotation();
                simpleTypeNode.addChild(annotation);

                // добавляем restriction
                XmlSchemaSimpleTypeRestriction restriction = (XmlSchemaSimpleTypeRestriction) ((XmlSchemaSimpleType) simpleTypeNode.data).getContent();
                simpleTypeNode.addChild(restriction);
            }

            // получим restriction
            TreeNode<Object> restrictionNode = simpleTypesNode.children.get(0).children.get(1);
            XmlSchemaSimpleTypeRestriction restriction = (XmlSchemaSimpleTypeRestriction) restrictionNode.data;

            // добавим все facets как листья в restriction
            for (XmlSchemaFacet facet : restriction.getFacets()) {
                restrictionNode.addChild(facet);
            }
        }
        return simpleTypesNode;
    }

    /**
     * Раскладывает ComplexType в дерево.
     */
    @Override
    public TreeNode<Object> createComplexTypeTree(XmlSchema xmlSchema) {
        TreeNode<Object> complexTypesNode = new TreeNode<Object>("ComplexType");

        // получим все ComplexType
        for (XmlSchemaComplexType complexType : SchemaHelper.getComplexTypeItems(xmlSchema)) {
            complexTypesNode.addChild(complexType);
        }

        // имеет ли смысл дальше работать
        if (complexTypesNode.children.size() != 0) {
            for (TreeNode<Object> complexTypeNode : complexTypesNode.children) {

                // описание ComplexType
                XmlSchemaAnnotation annotation = ((XmlSchemaComplexType) complexTypeNode.data).getAnnotation();
                complexTypeNode.addChild(annotation);

                // случай, когда нужно брать данные из ComplexContent
                if (((XmlSchemaComplexType) complexTypeNode.data).getContentModel() != null
                        && ((XmlSchemaComplexType) complexTypeNode.data).getParticle() == null) {

                    XmlSchemaComplexContentExtension extension =
                            (XmlSchemaComplexContentExtension) ((XmlSchemaComplexType) complexTypeNode.data).getContentModel().getContent();
                    complexTypeNode.addChild(extension);

                    // Получим ноду, где находится sequence, attribute, предполагается, что здесь нету choice
                    TreeNode<Object> contentExtensionNode = complexTypeNode.children.get(1);

                    XmlSchemaParticle sequence = ((XmlSchemaComplexContentExtension) (contentExtensionNode.data)).getParticle();
                    contentExtensionNode.addChild(sequence);

                    TreeNode<Object> sequenceNode = contentExtensionNode.children.get(0);

                    try {
                        // добавим в sequenceNode все элементы sequence как child
                        // с XmlSchemaChoice работаем при сравнении отдельно
                        for (XmlSchemaSequenceMember sequenceMember : ((XmlSchemaSequence) sequenceNode.data).getItems()) {
                            sequenceNode.addChild(sequenceMember);
                        }
                    } catch (NullPointerException e) {
                        // sequence может и не быть
                    }

                    // на том же уровне, что и sequence, аттрибута может и не быть
                    List<XmlSchemaAttributeOrGroupRef> attribute = ((XmlSchemaComplexContentExtension) contentExtensionNode.data).getAttributes();
                    contentExtensionNode.addChild(attribute);
                }

                // случай, когда в complexType нет ComplexContent, но есть sequence, данные берем из Particle
                if (((XmlSchemaComplexType) complexTypeNode.data).getParticle() != null
                        && ((XmlSchemaComplexType) complexTypeNode.data).getContentModel() == null) {

                    // sequenceNode на одном уровне с annotation
                    XmlSchemaSequence sequence = (XmlSchemaSequence) ((XmlSchemaComplexType) complexTypeNode.data).getParticle();
                    complexTypeNode.addChild(sequence);

                    TreeNode<Object> sequenceNode = complexTypeNode.children.get(1);

                    // добавим в sequenceNode все элементы sequence как child
                    // с XmlSchemaChoice работаем при сравнении отдельно
                    for (XmlSchemaSequenceMember sequenceMember : sequence.getItems()) {
                        sequenceNode.addChild(sequenceMember);
                    }
                }
            }
        }
        return complexTypesNode;
    }

    /**
     * Строит полное дерево схемы.
     */
    @Override
    public TreeNode<Object> buildSchemaTree(XmlSchema xmlSchema, String schemaPath) {

        // Root
        TreeNode<Object> schemaTree = new TreeNode<Object>(xmlSchema);

        // Schema description
        TreeNode<Object> description = createSchemaDescriptionTree(schemaPath);
        schemaTree.addChild(description);

        // Elements
        TreeNode<Object> elements = createSchemaElementsTree(xmlSchema);
        schemaTree.addChild(elements);

        // Imports
        TreeNode<Object> imports = createSchemaImportsTree(xmlSchema);
        schemaTree.addChild(imports);

        // SimpleTypes
        TreeNode<Object> simpleTypes = createSimpleTypeTree(xmlSchema);
        schemaTree.addChild(simpleTypes);

        // ComplexTypes
        TreeNode<Object> complexTypes = createComplexTypeTree(xmlSchema);
        schemaTree.addChild(complexTypes);

        return schemaTree;
    }
}
