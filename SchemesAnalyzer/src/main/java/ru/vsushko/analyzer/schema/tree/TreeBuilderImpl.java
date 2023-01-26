package ru.vsushko.analyzer.schema.tree;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAnnotation;
import org.apache.ws.commons.schema.XmlSchemaAttributeOrGroupRef;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSequenceMember;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import ru.vsushko.analyzer.schema.SchemaHelper;

import java.util.List;

/**
 * Created by vsa
 * Date: 18.12.14.
 */
public class TreeBuilderImpl implements TreeBuilder {

    /**
     * Creates schema description tree.
     */
    @Override
    public TreeNode<Object> createSchemaDescriptionTree(String schemaFullPath) {
        TreeNode<Object> schemaDescription = new TreeNode<>("SchemaName");
        schemaDescription.addChild(SchemaHelper.getSchemaDescription(schemaFullPath));
        return schemaDescription;
    }

    /**
     * Creates schema elements tree.
     */
    @Override
    public TreeNode<Object> createSchemaElementsTree(XmlSchema xmlSchema) {
        TreeNode<Object> schemaElement = new TreeNode<>("Elements");
        schemaElement.addChild(SchemaHelper.getElementsMap(xmlSchema));
        return schemaElement;
    }

    /**
     * Creates schema imports tree.
     */
    @Override
    public TreeNode<Object> createSchemaImportsTree(XmlSchema xmlSchema) {
        TreeNode<Object> schemaImports = new TreeNode<>("Imports");

        for (XmlSchemaImport schemaImport : SchemaHelper.getSchemaImports(xmlSchema)) {
            schemaImports.addChild(schemaImport);
        }
        return schemaImports;
    }

    /**
     * Turns out SimpleType into tree.
     */
    @Override
    public TreeNode<Object> createSimpleTypeTree(XmlSchema xmlSchema) {
        TreeNode<Object> simpleTypesNode = new TreeNode<>("SimpleType");

        // get all SimpleTypes
        for (XmlSchemaSimpleType simpleType : SchemaHelper.getSimpleTypeItems(xmlSchema)) {
            simpleTypesNode.addChild(simpleType);
        }

        // check for children existence
        if (simpleTypesNode.children.size() != 0) {
            for (TreeNode<Object> simpleTypeNode : simpleTypesNode.children) {

                // add annotation
                XmlSchemaAnnotation annotation = ((XmlSchemaSimpleType) simpleTypeNode.data).getAnnotation();
                simpleTypeNode.addChild(annotation);

                // add restriction
                XmlSchemaSimpleTypeRestriction restriction = (XmlSchemaSimpleTypeRestriction) ((XmlSchemaSimpleType) simpleTypeNode.data).getContent();
                simpleTypeNode.addChild(restriction);
            }

            // get restriction
            TreeNode<Object> restrictionNode = simpleTypesNode.children.get(0).children.get(1);
            XmlSchemaSimpleTypeRestriction restriction = (XmlSchemaSimpleTypeRestriction) restrictionNode.data;

            // add facets as leaves in в restriction
            for (XmlSchemaFacet facet : restriction.getFacets()) {
                restrictionNode.addChild(facet);
            }
        }
        return simpleTypesNode;
    }

    /**
     * Turns out ComplexType into tree.
     */
    @Override
    public TreeNode<Object> createComplexTypeTree(XmlSchema xmlSchema) {
        TreeNode<Object> complexTypesNode = new TreeNode<>("ComplexType");

        // get all ComplexTypes
        for (XmlSchemaComplexType complexType : SchemaHelper.getComplexTypeItems(xmlSchema)) {
            complexTypesNode.addChild(complexType);
        }

        // check for children existence
        if (complexTypesNode.children.size() != 0) {
            for (TreeNode<Object> complexTypeNode : complexTypesNode.children) {

                // ComplexType description
                XmlSchemaAnnotation annotation = ((XmlSchemaComplexType) complexTypeNode.data).getAnnotation();
                complexTypeNode.addChild(annotation);

                // case, when we should retrieve data from ComplexContent
                if (((XmlSchemaComplexType) complexTypeNode.data).getContentModel() != null && ((XmlSchemaComplexType) complexTypeNode.data).getParticle() == null) {

                    XmlSchemaComplexContentExtension extension = (XmlSchemaComplexContentExtension) ((XmlSchemaComplexType) complexTypeNode.data).getContentModel().getContent();
                    complexTypeNode.addChild(extension);

                    // get node with sequence attribute without choice
                    TreeNode<Object> contentExtensionNode = complexTypeNode.children.get(1);

                    XmlSchemaParticle sequence = ((XmlSchemaComplexContentExtension) (contentExtensionNode.data)).getParticle();
                    contentExtensionNode.addChild(sequence);

                    TreeNode<Object> sequenceNode = contentExtensionNode.children.get(0);

                    try {
                        // adding sequenceNode to all elements as a child
                        // XmlSchemaChoice treatment would be implemented separately
                        for (XmlSchemaSequenceMember sequenceMember : ((XmlSchemaSequence) sequenceNode.data).getItems()) {
                            sequenceNode.addChild(sequenceMember);
                        }
                    } catch (NullPointerException e) {
                        // there is no sequence
                    }

                    // at the same level as sequence, the attribute could not be existed
                    List<XmlSchemaAttributeOrGroupRef> attribute = ((XmlSchemaComplexContentExtension) contentExtensionNode.data).getAttributes();
                    contentExtensionNode.addChild(attribute);
                }

                // case, when complexType has no ComplexContent, but sequence has, take data from Particle
                if (((XmlSchemaComplexType) complexTypeNode.data).getParticle() != null && ((XmlSchemaComplexType) complexTypeNode.data).getContentModel() == null) {

                    // sequenceNode at the same level as annotation
                    XmlSchemaSequence sequence = (XmlSchemaSequence) ((XmlSchemaComplexType) complexTypeNode.data).getParticle();
                    complexTypeNode.addChild(sequence);

                    TreeNode<Object> sequenceNode = complexTypeNode.children.get(1);

                    // add to sequenceNode all elements as child
                    // XmlSchemaChoice treatment would be implemented separately
                    for (XmlSchemaSequenceMember sequenceMember : sequence.getItems()) {
                        sequenceNode.addChild(sequenceMember);
                    }
                }
            }
        }
        return complexTypesNode;
    }

    /**
     * Creates complete schema tree.
     */
    @Override
    public TreeNode<Object> buildSchemaTree(XmlSchema xmlSchema, String schemaPath) {

        // Root
        TreeNode<Object> schemaTree = new TreeNode<>(xmlSchema);

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
