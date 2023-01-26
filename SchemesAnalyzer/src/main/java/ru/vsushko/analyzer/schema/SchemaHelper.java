package ru.vsushko.analyzer.schema;

import com.sun.org.apache.xerces.internal.dom.DeferredTextImpl;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaAttributeOrGroupRef;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaChoiceMember;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaDocumentation;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaEnumerationFacet;
import org.apache.ws.commons.schema.XmlSchemaException;
import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.apache.ws.commons.schema.XmlSchemaFractionDigitsFacet;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.apache.ws.commons.schema.XmlSchemaLengthFacet;
import org.apache.ws.commons.schema.XmlSchemaMaxExclusiveFacet;
import org.apache.ws.commons.schema.XmlSchemaMaxInclusiveFacet;
import org.apache.ws.commons.schema.XmlSchemaMaxLengthFacet;
import org.apache.ws.commons.schema.XmlSchemaMinExclusiveFacet;
import org.apache.ws.commons.schema.XmlSchemaMinInclusiveFacet;
import org.apache.ws.commons.schema.XmlSchemaMinLengthFacet;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaPatternFacet;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSequenceMember;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.apache.ws.commons.schema.XmlSchemaTotalDigitsFacet;
import org.apache.ws.commons.schema.XmlSchemaWhiteSpaceFacet;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import ru.vsushko.analyzer.schema.complextype.attribute.Attribute;
import ru.vsushko.analyzer.schema.complextype.attribute.AttributeImpl;
import ru.vsushko.analyzer.schema.complextype.sequence.Element;
import ru.vsushko.analyzer.schema.complextype.sequence.ElementImpl;
import ru.vsushko.analyzer.schema.tree.TreeHelper;
import ru.vsushko.analyzer.schema.tree.TreeNode;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vsa
 * Date: 08.12.14.
 */
public class SchemaHelper {

    private SchemaHelper() throws Exception {
        throw new Exception("Could not init");
    }

    /**
     * Returns schema from specified path.
     */
    public static XmlSchema getSchemaFromPath(String schemaPath) {
        try {
            XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
            Source source = new StreamSource(new File(schemaPath));
            return schemaCollection.read(source);
        } catch (XmlSchemaException e) {
            return null;
        }
    }

    /**
     * Takes via XPath first occurrence
     * <xs:documentation><xs:annotation>Schema description</xs:annotation></xs:documentation>.
     */
    public static String getSchemaDescription(String pathToSchema) {
        String schemaDescription;
        try {
            FileInputStream file = new FileInputStream(pathToSchema);
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document xmlDocument = builder.parse(file);
            XPath xPath = XPathFactory.newInstance().newXPath();

            String expression = "//annotation/documentation";
            Node node = (Node) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODE);
            schemaDescription = ((DeferredTextImpl) node.getFirstChild()).getData();
            return schemaDescription;
        } catch (ParserConfigurationException e) {
            System.out.println("Can't create parser ");
        } catch (XPathExpressionException e) {
            System.out.println("Can't read XPath expression");
        } catch (FileNotFoundException e) {
            System.out.println("Cannot find file " + pathToSchema);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns schema version.
     */
    public static String readSchemaVersionInfo(String schemaTargetNamespace) {
        Pattern pattern = Pattern.compile("\\d.{2,}");
        Matcher matcher = pattern.matcher(schemaTargetNamespace);
        return matcher.find() ? matcher.group() : null;
    }

    /**
     * Returns ComplexTypes list.
     */
    public static List<String> getComplexTypeListFromSchema(XmlSchema xmlSchema) {
        List<String> complexTypeNames = new ArrayList<>();

        // put into list all ComplexType names
        for (QName type : new ArrayList<>(xmlSchema.getSchemaTypes().keySet())) {
            if (xmlSchema.getSchemaTypes().get(type) instanceof XmlSchemaComplexType) {
                complexTypeNames.add(type.getLocalPart());
            }
        }
        return complexTypeNames;
    }

    /**
     * Returns SimpleType list.
     */
    public static List<String> getSimpleTypeListFromSchema(XmlSchema xmlSchema) {
        List<String> simpleTypeNames = new ArrayList<>();

        // put into list all SimpleType names
        for (QName type : new ArrayList<>(xmlSchema.getSchemaTypes().keySet())) {
            if (xmlSchema.getSchemaTypes().get(type) instanceof XmlSchemaSimpleType) {
                simpleTypeNames.add(type.getLocalPart());
            }
        }
        return simpleTypeNames;
    }

    /**
     * Returns new ComplexType list.
     */
    public static List<String> getNewComplexTypes(List<String> actualTypeNames, List<String> typeNamesToCompare) {
        List<String> newComplexTypeNames = new ArrayList<>(typeNamesToCompare);

        for (String typeName : actualTypeNames) {
            newComplexTypeNames.remove(typeName);
        }
        return newComplexTypeNames;
    }

    /**
     * Returns all ComplexType list which were exited earlier.
     */
    public static List<String> getPreExistingComplexTypes(List<String> actualTypeNames, List<String> typeNamesToCompare) {
        List<String> preExistingTypeNames = new ArrayList<>(actualTypeNames);

        for (String typeName : typeNamesToCompare) {
            preExistingTypeNames.remove(typeName);
        }
        return preExistingTypeNames;
    }

    /**
     * Returns new SimpleType list.
     */
    public static List<String> getNewSimpleTypes(List<String> actualTypeNames, List<String> typeNamesToCompare) {
        List<String> newSimpleTypeNames = new ArrayList<>(typeNamesToCompare);

        for (String typeName : actualTypeNames) {
            newSimpleTypeNames.remove(typeName);
        }
        return newSimpleTypeNames;
    }

    /**
     * Returns all SimpleType list which were exited earlier.
     */
    public static List<String> getPreExistingSimpleTypes(List<String> actualTypeNames, List<String> typeNamesToCompare) {
        List<String> preExistingElements = new ArrayList<>(actualTypeNames);

        for (String typeName : typeNamesToCompare) {
            preExistingElements.remove(typeName);
        }
        return preExistingElements;
    }

    /**
     * Returns difference between ComplexType elements.
     */
    public static List<String> getDifferenceBetweenComplexTypes(List<XmlSchemaComplexType> oldComplexTypes, List<XmlSchemaComplexType> newComplexTypes) {
        List<String> differences = new ArrayList<>();

        if (oldComplexTypes.size() != newComplexTypes.size()) {
            try {
                throw new Exception("Complex type lists have the length difference...");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (int idx = 0; idx < oldComplexTypes.size() && idx < newComplexTypes.size(); idx++) {
            String complexTypeName = newComplexTypes.get(idx).getName();

            // check ComplexTypeAnnotation
            String oldAnnotationDescription = SchemaHelper.getComplexTypeAnnotation(oldComplexTypes.get(idx));
            String newAnnotationDescription = SchemaHelper.getComplexTypeAnnotation(newComplexTypes.get(idx));

            // case without ComplexTypeAnnotations
            if (oldAnnotationDescription != null && newAnnotationDescription != null && !oldAnnotationDescription.equals(newAnnotationDescription)) {
                differences.add("\n" + newComplexTypes.get(idx).getName() + " changed description: " + newAnnotationDescription + "\n");
            }

            // case when data from ComplexContent should be taken (global types)
            if (newComplexTypes.get(idx).getContentModel() != null && oldComplexTypes.get(idx).getContentModel() != null && newComplexTypes.get(idx).getParticle() == null && oldComplexTypes.get(idx).getParticle() == null) {

                XmlSchemaComplexContentExtension oldExtension = (XmlSchemaComplexContentExtension) oldComplexTypes.get(idx).getContentModel().getContent();
                XmlSchemaComplexContentExtension newExtension = (XmlSchemaComplexContentExtension) newComplexTypes.get(idx).getContentModel().getContent();

                // checking SequencesElements
                List<String> sequenceChanges = SchemaHelper.getDifferenceBetweenExtensionSequencesElements(oldExtension, newExtension);

                if (sequenceChanges.size() != 0) {
                    if (!differences.contains(complexTypeName)) {
                        differences.add(complexTypeName + "\n");
                    }
                    for (String diff : sequenceChanges) {
                        differences.add(" " + diff + "\n");
                    }
                }

                List<XmlSchemaAttributeOrGroupRef> oldAttributes = oldExtension.getAttributes();
                List<XmlSchemaAttributeOrGroupRef> newAttributes = newExtension.getAttributes();

                List<String> attributeChanges = SchemaHelper.getDifferenceBetweenExtensionAttributesValues(oldAttributes, newAttributes);

                if (attributeChanges != null && attributeChanges.size() != 0) {
                    if (!differences.contains(complexTypeName)) {
                        differences.add(complexTypeName + "\n");
                    }
                    for (String diff : attributeChanges) {
                        differences.add(" " + diff + "\n");
                    }
                }
            }
            // case when data from Particle should be taken (local types )
            if (newComplexTypes.get(idx).getParticle() != null && oldComplexTypes.get(idx).getParticle() != null && newComplexTypes.get(idx).getContentModel() == null && oldComplexTypes.get(idx).getContentModel() == null) {

                XmlSchemaSequence oldExtension = (XmlSchemaSequence) oldComplexTypes.get(idx).getParticle();
                XmlSchemaSequence newExtension = (XmlSchemaSequence) newComplexTypes.get(idx).getParticle();

                // compare SequencesElements
                List<String> sequenceChanges = SchemaHelper.getDifferenceBetweenExtensionSequencesElements(oldExtension.getItems(), newExtension.getItems());

                if (sequenceChanges.size() != 0) {
                    if (!differences.contains(complexTypeName)) {
                        differences.add(complexTypeName + "\n");
                    }
                    for (String diff : sequenceChanges) {
                        differences.add(" " + diff + "\n");
                    }
                }

                List<XmlSchemaAttributeOrGroupRef> oldAttributes = oldComplexTypes.get(idx).getAttributes();
                List<XmlSchemaAttributeOrGroupRef> newAttributes = newComplexTypes.get(idx).getAttributes();

                List<String> attributeChanges = SchemaHelper.getDifferenceBetweenExtensionAttributesValues(oldAttributes, newAttributes);

                if (attributeChanges != null && attributeChanges.size() != 0) {
                    if (!differences.contains(complexTypeName)) {
                        differences.add(complexTypeName + "\n");
                    }
                    for (String diff : attributeChanges) {
                        differences.add(" " + diff + "\n");
                    }
                }
            }
        }
        return differences;
    }

    /**
     * Returns changes between Elements and Choice in XmlSchemaComplexContentExtension.
     */
    private static List<String> getDifferenceBetweenExtensionSequencesElements(XmlSchemaComplexContentExtension oldExtension, XmlSchemaComplexContentExtension newExtension) {
        List<String> difference = new ArrayList<>();

        List<XmlSchemaElement> oldElements = SchemaHelper.getSequenceElements(oldExtension);
        List<XmlSchemaElement> newElements = SchemaHelper.getSequenceElements(newExtension);

        // possible existence of extension and elements
        if (oldElements != null && newElements != null) {
            // added new XmlSchemaElement elements
            difference.addAll(SchemaHelper.getNewElementsInComplexTypeSequenceElements(oldElements, newElements));
            // removed XmlSchemaElement elementss
            difference.addAll(SchemaHelper.getRemovedElementsInComplexTypeSequenceElements(oldElements, newElements));

            // get list with ElementsNames from old schema
            List<String> oldElementsNames = SchemaHelper.getElementsNames(oldElements);

            // get list with ElementsNames from new schema
            List<String> newElementsNames = SchemaHelper.getElementsNames(newElements);

            // get list XmlSchemaElement with same names
            List<String> sameSchemaElementsNames = SchemaHelper.getSameTypeNames(oldElementsNames, newElementsNames);

            List<XmlSchemaElement> oldSameElements = SchemaHelper.getElementsFromComplexTypeBySpecificNames(oldElements, sameSchemaElementsNames);
            List<XmlSchemaElement> newSameElements = SchemaHelper.getElementsFromComplexTypeBySpecificNames(newElements, sameSchemaElementsNames);

            // fill DifferenceBetweenExtensionSequencesElementsValues list between extensions
            difference.addAll(SchemaHelper.getDifferenceBetweenExtensionSequencesElementsValues(oldSameElements, newSameElements));

            // if there is choice elements - analyze them
            List<XmlSchemaChoice> oldChoices = SchemaHelper.getSequenceChoices(oldExtension);
            List<XmlSchemaChoice> newChoices = SchemaHelper.getSequenceChoices(newExtension);

            if (oldChoices.size() != 0 && oldChoices.size() != 0) {
                difference.addAll(SchemaHelper.getDifferenceBetweenChoices(oldChoices, newChoices));
            }

            // removed XmlSchemaChoice element
            if (oldChoices.size() != 0 && newChoices.size() == 0) {
                // get description
                difference.add("removed choice element");
            }

            // added new XmlSchemaChoice
            if (oldChoices.size() == 0 && newChoices.size() != 0) {
                // get description
                difference.add("added new choice element");
            }
        }
        return difference;
    }

    private static List<String> getDifferenceBetweenChoices(List<XmlSchemaChoice> oldChoices, List<XmlSchemaChoice> newChoices) {
        List<String> difference = new ArrayList<>();

        String newDescription;
        // added description
        if (oldChoices.size() == 0 && newChoices.size() == 1) {
            for (XmlSchemaChoice choice : newChoices) {
                newDescription = SchemaHelper.getChoiceAnnotation(choice);
                if (newDescription != null) {
                    difference.add("in choice element added new description " + newDescription);
                }
            }
        }
        String oldDescription;
        // removed description
        if (oldChoices.size() == 1 && newChoices.size() == 0) {
            for (XmlSchemaChoice choice : oldChoices) {
                oldDescription = SchemaHelper.getChoiceAnnotation(choice);
                if (oldDescription != null) {
                    difference.add("in choice element removed description " + oldDescription);
                }
            }
        }
        // if there are choice element then compare them
        if (oldChoices.size() == 1 && newChoices.size() == 1) {
            // compare descriptions
            newDescription = SchemaHelper.getChoiceAnnotation(newChoices.get(0));
            oldDescription = SchemaHelper.getChoiceAnnotation(oldChoices.get(0));

            if (newDescription != null && oldDescription != null && !oldDescription.equals(newDescription)) {
                difference.add("Choice description element was changed : \n" + newDescription);
            }

            // id=ID
            String oldId = oldChoices.get(0).getId();
            String newId = newChoices.get(0).getId();
            if (oldId != null && newId != null && !oldId.equals(newId)) {
                difference.add("Choice 'id' element value was changed from " + oldId + " to " + newId);
            }

            // TODO: fix choice difference output

            // maxOccurs=nonNegativeInteger|unbounded
            String oldMaxOccurs = String.valueOf(oldChoices.get(0).getMaxOccurs());
            String newMaxOccurs = String.valueOf(newChoices.get(0).getMaxOccurs());
            if (!oldMaxOccurs.equals(newMaxOccurs)) {
                difference.add("Choice 'maxOccurs' element value changed from  " + oldMaxOccurs + " to " + newMaxOccurs);
            }

            // minOccurs=nonNegativeInteger
            String oldMinOccurs = String.valueOf(oldChoices.get(0).getMinOccurs());
            String newMinOccurs = String.valueOf(newChoices.get(0).getMinOccurs());
            if (!oldMinOccurs.equals(newMinOccurs)) {
                difference.add("Choice 'minOccurs' element value changed from " + oldMinOccurs + " to " + newMinOccurs);
            }

            List<XmlSchemaElement> oldChoiceElements = SchemaHelper.getChoicesElements(oldChoices);
            List<XmlSchemaElement> newChoiceElements = SchemaHelper.getChoicesElements(newChoices);

            // added new XmlSchemaElement elements
            difference.addAll(SchemaHelper.getNewElementsInComplexTypeSequenceElements(oldChoiceElements, newChoiceElements));
            // removed old XmlSchemaElement elements
            difference.addAll(SchemaHelper.getRemovedElementsInComplexTypeSequenceElements(oldChoiceElements, newChoiceElements));

            // get old choice elements names
            List<String> oldElementsNames = SchemaHelper.getElementsNames(oldChoiceElements);

            // get new choice elements names
            List<String> newElementsNames = SchemaHelper.getElementsNames(newChoiceElements);

            // get XmlSchemaChoice list with the same names
            List<String> sameSchemaElementsNames = SchemaHelper.getSameTypeNames(oldElementsNames, newElementsNames);

            List<XmlSchemaElement> oldSameElements = SchemaHelper.getElementsFromComplexTypeBySpecificNames(oldChoiceElements, sameSchemaElementsNames);
            List<XmlSchemaElement> newSameElements = SchemaHelper.getElementsFromComplexTypeBySpecificNames(newChoiceElements, sameSchemaElementsNames);

            difference.addAll(SchemaHelper.getDifferenceBetweenExtensionSequencesElementsValues(oldSameElements, newSameElements));
        }
        return difference;
    }

    /**
     * Returns list of Choice elements.
     */
    private static List<XmlSchemaElement> getChoicesElements(List<XmlSchemaChoice> choices) {
        List<XmlSchemaElement> elements = new ArrayList<>();

        // work only with firsts choice elements occurrence
        if (choices.size() == 1) {
            for (XmlSchemaChoice choice : choices) {
                for (XmlSchemaChoiceMember member : choice.getItems()) {
                    // and only with XmlSchemaElements inside, it could be possible to make an implementation with sequence
                    // (annotation?,(element|group|choice|sequence|any)*)
                    if (member instanceof XmlSchemaElement) {
                        elements.add((XmlSchemaElement) member);
                    }
                }
            }
        }
        return elements;
    }

    /**
     * Returns changes between ComplexType elements, works with XmlSchemaSequenceMember.
     */
    private static List<String> getDifferenceBetweenExtensionSequencesElements(List<XmlSchemaSequenceMember> oldMemberList, List<XmlSchemaSequenceMember> newMemberList) {
        List<String> difference = new ArrayList<>();

        List<XmlSchemaElement> oldElements = SchemaHelper.getSequenceElements(oldMemberList);
        List<XmlSchemaElement> newElements = SchemaHelper.getSequenceElements(newMemberList);

        // extensions couldn't be elements too
        if (oldElements != null && newElements != null) {
            // added new XmlSchemaElement elements
            difference.addAll(SchemaHelper.getNewElementsInComplexTypeSequenceElements(oldElements, newElements));
            // removed old XmlSchemaElement elements
            difference.addAll(SchemaHelper.getRemovedElementsInComplexTypeSequenceElements(oldElements, newElements));

            // get list with old schema elements
            List<String> oldElementsNames = SchemaHelper.getElementsNames(oldElements);

            // get list with new schema elements
            List<String> newElementsNames = SchemaHelper.getElementsNames(newElements);

            // get XmlSchemaElement list with the same names
            List<String> sameSchemaElementsNames = SchemaHelper.getSameTypeNames(oldElementsNames, newElementsNames);

            List<XmlSchemaElement> oldSameElements = SchemaHelper.getElementsFromComplexTypeBySpecificNames(oldElements, sameSchemaElementsNames);
            List<XmlSchemaElement> newSameElements = SchemaHelper.getElementsFromComplexTypeBySpecificNames(newElements, sameSchemaElementsNames);

            difference.addAll(SchemaHelper.getDifferenceBetweenExtensionSequencesElementsValues(oldSameElements, newSameElements));

            // if there ara choice, analyze them
            List<XmlSchemaChoice> oldChoices = SchemaHelper.getSequenceChoices(oldMemberList);
            List<XmlSchemaChoice> newChoices = SchemaHelper.getSequenceChoices(newMemberList);

            if (oldChoices.size() != 0 && oldChoices.size() != 0) {
                difference.addAll(SchemaHelper.getDifferenceBetweenChoices(oldChoices, newChoices));
            }

            // removed old XmlSchemaChoice
            if (oldChoices.size() != 0 && newChoices.size() == 0) {
                // get description
                difference.add("Choice element was deleted");
            }

            // added new XmlSchemaChoice
            if (oldChoices.size() == 0 && newChoices.size() != 0) {
                // get description
                difference.add("Choice new element was added");
            }
        }
        return difference;
    }

    /**
     * Returns XmlSchemaElement with specified name.
     */
    public static XmlSchemaElement getXmlSchemaElementWithSameName(List<XmlSchemaElement> elements, String name) {
        Element elementHelper = new ElementImpl();
        for (XmlSchemaElement schemaElement : elements) {
            if (elementHelper.getName(schemaElement).equals(name)) {
                return schemaElement;
            }
        }
        return null;
    }

    /**
     * Returns the difference between extension sequences elements values.
     */
    private static List<String> getDifferenceBetweenExtensionSequencesElementsValues(List<XmlSchemaElement> oldSameElements, List<XmlSchemaElement> newSameElements) {
        List<String> diffStrings = new ArrayList<>();

        Element element = new ElementImpl();

        if (oldSameElements.size() == newSameElements.size()) {
            for (XmlSchemaElement newSameElement : newSameElements) {
                // get element from old schema with that same name
                XmlSchemaElement oldSameElement = getXmlSchemaElementWithSameName(oldSameElements, element.getName(newSameElement));

                // compare elements attributes
                if (newSameElement != null && oldSameElement != null) {

                    // type
                    if (!element.getType(oldSameElement).equals(element.getType(newSameElement))) {
                        diffStrings.add("For element " + element.getName(newSameElement) + " type was changed to " + element.getType(newSameElement));
                    }

                    /*if (!element.getPrefix(oldSameElement).equals(element.getPrefix(newSameElement))) {
                        diffStrings.add("'prefix' attribute value was changed from " + element.getPrefix(oldSameElement) + " to " + element.getPrefix(newSameElement));
                    }*/

                    // TODO: work with unbounded value
                    // case when minOccurs and maxOccurs was changed at the same time
                    if (!element.getMinOccurs(oldSameElement).equals(element.getMinOccurs(newSameElement)) && !element.getMaxOccurs(oldSameElement).equals(element.getMaxOccurs(newSameElement))) {
                        // if m=M, then print only [M]
                        if (element.getMinOccurs(newSameElement).equals(element.getMaxOccurs(newSameElement))) {
                            diffStrings.add("For element " + element.getName(newSameElement) + " multiplicity changed [" + element.getMaxOccurs(newSameElement) + "]");
                        } else {
                            diffStrings.add("For element " + element.getName(newSameElement) + " multiplicity changed [" + element.getMinOccurs(newSameElement) + ".." + element.getMaxOccurs(newSameElement) + "]");
                        }
                    } else {
                        // preceding MinOccurs value was 1, now - it is another value
                        if (element.getMinOccurs(oldSameElement).equals("1") && !element.getMinOccurs(oldSameElement).equals(element.getMinOccurs(newSameElement))) {
                            // if m=M=1, then [1]
                            if (element.getMinOccurs(newSameElement).equals("1") && element.getMaxOccurs(newSameElement).equals("1")) {
                                diffStrings.add("For element " + element.getName(newSameElement) + " changed mandatory [1]");
                            } else {
                                diffStrings.add("For element " + element.getName(newSameElement) + " changed mandatory [m.." + element.getMaxOccurs(newSameElement) + "]");
                            }
                        }

                        // preceding MaxOccurs value was  1, now - it is another value
                        if (element.getMaxOccurs(oldSameElement).equals("1") && !element.getMaxOccurs(oldSameElement).equals(element.getMaxOccurs(newSameElement))) {
                            // if m=M=1, then [1]
                            if (element.getMinOccurs(newSameElement).equals("1") && element.getMaxOccurs(newSameElement).equals("1")) {
                                diffStrings.add("For element " + element.getName(newSameElement) + " multiplicity changed [1]");
                            } else {
                                diffStrings.add("For element " + element.getName(newSameElement) + " multiplicity changed [" + element.getMinOccurs(newSameElement) + "..M]");
                            }
                        }

                        // preceding MinOccurs was declared, now - it is value 1
                        if (element.getMinOccurs(newSameElement).equals("1") && !element.getMinOccurs(oldSameElement).equals(element.getMinOccurs(newSameElement))) {
                            // if M=1, then [1]
                            if (element.getMaxOccurs(newSameElement).equals("1")) {
                                diffStrings.add("For element " + element.getName(newSameElement) + " changed mandatory [1]");
                            } else {
                                // MaxOccurs could have value "0"
                                diffStrings.add("For element " + element.getName(newSameElement) + " changed mandatory [1.." + element.getMaxOccurs(newSameElement) + "]");
                            }
                        }

                        // preceding MaxOccurs was declared, now - it is value 1
                        if (element.getMaxOccurs(newSameElement).equals("1") && !element.getMaxOccurs(oldSameElement).equals(element.getMaxOccurs(newSameElement))) {
                            // if m=1, then [1]
                            if (element.getMinOccurs(newSameElement).equals("1")) {
                                diffStrings.add("For element " + element.getName(newSameElement) + " multiplicity changed [1]");
                            } else {
                                diffStrings.add("For element " + element.getName(newSameElement) + " multiplicity changed [" + element.getMinOccurs(newSameElement) + "..1]");
                            }
                        }
                    }

                    // %-)

                    // description of element could not declared
                    String oldDescription = element.getDescription(oldSameElement);
                    String newDescription = element.getDescription(newSameElement);
                    if (oldDescription != null && newDescription != null && !oldDescription.equals(element.getDescription(newSameElement))) {
                        diffStrings.add(" For element " + element.getName(newSameElement) + " changed description: " + element.getDescription(newSameElement));
                    }
                }
            }
        }
        return diffStrings;
    }

    private static List<String> getElementsNames(List<XmlSchemaElement> elements) {
        List<String> elementsNames = new ArrayList<>();

        for (XmlSchemaElement element : elements) {
            elementsNames.add(element.getName());
        }
        return elementsNames;
    }

    /**
     * Returns changes between atributess in XmlSchemaComplexContentExtension.
     */
    private static List<String> getDifferenceBetweenExtensionAttributesValues(List<XmlSchemaAttributeOrGroupRef> oldAttributes, List<XmlSchemaAttributeOrGroupRef> newAttributes) {
        List<String> diffStrings = new ArrayList<>();

        // TODO: comparison with null values should be refactored

        // if size of collection are equal
        if (oldAttributes.size() == newAttributes.size()) {
            for (int idx = 0; idx < oldAttributes.size() && idx < newAttributes.size(); idx++) {

                // compare attributes
                Attribute attribute = new AttributeImpl();

                XmlSchemaAttribute oldAttribute = (XmlSchemaAttribute) oldAttributes.get(idx);
                XmlSchemaAttribute newAttribute = (XmlSchemaAttribute) newAttributes.get(idx);

                if (oldAttribute != null && newAttribute != null) {

                    String oldFixedValue = attribute.getFixedValue(oldAttribute);
                    String newFixedValue = attribute.getFixedValue(newAttribute);

                    if (oldFixedValue != null && newFixedValue != null && !oldFixedValue.equals(newFixedValue)) {
                        diffStrings.add("'fixed' value was changed in attribute " + attribute.getName(newAttribute) + " from " + oldFixedValue + " to " + newFixedValue);
                    }

                    String oldName = attribute.getName(oldAttribute);
                    String newName = attribute.getName(newAttribute);

                    if (oldName != null && newName != null && !oldName.equals(newName)) {
                        diffStrings.add("'name' value was changed in attribute from" + oldName + " to " + newName);
                    }

                    String oldPrefix = attribute.getPrefix(oldAttribute);
                    String newPrefix = attribute.getPrefix(newAttribute);

                    if (oldPrefix != null && newPrefix != null && !oldPrefix.equals(newPrefix)) {
                        diffStrings.add("'prefix' value was changed in attribute " + newName + " from " + oldPrefix + " to " + newPrefix);
                    }

                    String oldType = attribute.getType(oldAttribute);
                    String newType = attribute.getType(newAttribute);

                    if (oldType != null && newType != null && !oldType.equals(newType)) {
                        diffStrings.add("'type' value was changed in attribute " + attribute.getName(newAttribute) + " from " + oldType + " to " + newType);
                    }

                    String oldRequiredValue = attribute.getRequiredValue(oldAttribute);
                    String newRequiredValue = attribute.getRequiredValue(newAttribute);

                    if (!oldRequiredValue.equals(newRequiredValue)) {
                        diffStrings.add("'use' value was changed in attribute " + attribute.getName(newAttribute) + " from " + oldRequiredValue + " to " + newRequiredValue);
                    }

                    String oldDescription = attribute.getDescription(oldAttribute);
                    String newDescription = attribute.getDescription(newAttribute);

                    if (oldDescription != null && newDescription != null && !oldDescription.equals(newDescription)) {
                        diffStrings.add("'description' value was changed in attribute " + attribute.getName(newAttribute) + ": " + attribute.getDescription(newAttribute));
                    }
                }
            }
        }
        return diffStrings;
    }

    /**
     * Returns ComplexTypes list.
     */
    public static List<XmlSchemaComplexType> getComplexTypeItems(XmlSchema xmlSchema) {
        List<XmlSchemaComplexType> schemaComplexTypes = new ArrayList<>();

        for (XmlSchemaObject schemaObject : xmlSchema.getItems()) {
            if (schemaObject instanceof XmlSchemaComplexType) {
                schemaComplexTypes.add((XmlSchemaComplexType) schemaObject);
            }
        }
        return schemaComplexTypes;
    }

    /**
     * Returns SimpleType list.
     */
    public static List<XmlSchemaSimpleType> getSimpleTypeItems(XmlSchema xmlSchema) {
        List<XmlSchemaSimpleType> schemaSimpleTypes = new ArrayList<>();

        for (XmlSchemaObject schemaObject : xmlSchema.getItems()) {
            if (schemaObject instanceof XmlSchemaSimpleType) {
                schemaSimpleTypes.add((XmlSchemaSimpleType) schemaObject);
            }
        }
        return schemaSimpleTypes;
    }

    /**
     * Returns new sequence elements.
     */
    private static List<String> getNewElementsInComplexTypeSequenceElements(List<XmlSchemaElement> oldElements, List<XmlSchemaElement> newElements) {
        List<String> diffs = new ArrayList<>();

        // get list with old element names
        List<String> oldElementNames = new ArrayList<>();
        for (XmlSchemaElement schemaElement : oldElements) {
            oldElementNames.add(schemaElement.getName());
        }

        // get list with new element names
        List<String> newElementNames = new ArrayList<>();
        for (XmlSchemaElement schemaElement : newElements) {
            newElementNames.add(schemaElement.getName());
        }

        List<String> addedElements = SchemaHelper.getAddedElements(oldElementNames, newElementNames);

        for (XmlSchemaElement element : newElements) {
            if (addedElements.contains(element.getName())) {
                diffs.add("A new element was added " + element.getName() + "\n Description: " + getElementTypeAnnotation(element));
            }
        }
        return diffs;
    }

    /**
     * Returns list of added elements.
     */
    public static List<String> getAddedElements(List<String> oldElementsNames, List<String> newElementsNames) {
        List<String> addedElements = new ArrayList<>();

        for (String elementName : newElementsNames) {
            // if element was found in list then it is new element
            if (!oldElementsNames.contains(elementName)) {
                addedElements.add(elementName);
            }
        }
        return addedElements;
    }

    /**
     * Returns removed elements in ComplexType sequence.
     */
    private static List<String> getRemovedElementsInComplexTypeSequenceElements(List<XmlSchemaElement> oldElements, List<XmlSchemaElement> newElements) {
        List<String> diffs = new ArrayList<>();

        List<String> oldElementNames = new ArrayList<>();
        for (XmlSchemaElement schemaElement : oldElements) {
            oldElementNames.add(schemaElement.getName());
        }

        List<String> newElementNames = new ArrayList<>();
        for (XmlSchemaElement schemaElement : newElements) {
            newElementNames.add(schemaElement.getName());
        }

        List<String> removedElements = SchemaHelper.getRemovedElements(oldElementNames, newElementNames);

        for (XmlSchemaElement element : oldElements) {
            if (removedElements.contains(element.getName())) {
                diffs.add("Removed element " + element.getName() + "\n Description: " + getElementTypeAnnotation(element));
            }
        }
        return diffs;
    }

    /**
     * Returns list of removed elements.
     */
    public static List<String> getRemovedElements(List<String> oldElementsNames, List<String> newElementsNames) {
        List<String> removedElements = new ArrayList<>();

        for (String elementName : oldElementsNames) {
            if (!newElementsNames.contains(elementName)) {
                removedElements.add(elementName);
            }
        }
        return removedElements;
    }

    /**
     * Returns Annotation from Choice.
     */
    public static String getChoiceAnnotation(XmlSchemaChoice choice) {
        String description;
        try {
            description = ((XmlSchemaDocumentation) choice.getAnnotation().getItems().get(0)).getMarkup().item(0).getNodeValue();
            return description;
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * Returns Annotation from Element.
     */
    public static String getElementTypeAnnotation(XmlSchemaElement element) {
        return ((XmlSchemaDocumentation) element.getAnnotation().getItems().get(0)).getMarkup().item(0).getNodeValue();
    }

    /**
     * Returns Annotation from ComplexType.
     */
    public static String getComplexTypeAnnotation(XmlSchemaComplexType complexType) {
        String description;
        try {
            description = ((XmlSchemaDocumentation) complexType.getAnnotation().getItems().get(0)).getMarkup().item(0).getNodeValue();
            return description;
        } catch (NullPointerException ignore) {
        }
        return null;
    }

    /**
     * Returns Annotation from SimpleType.
     */
    public static String getSimpleTypeAnnotation(XmlSchemaSimpleType simpleType) {
        return ((XmlSchemaDocumentation) simpleType.getAnnotation().getItems().get(0)).getMarkup().item(0).getNodeValue();
    }

    /**
     * Returns all imports from schema.
     */
    public static List<XmlSchemaImport> getSchemaImports(XmlSchema schema) {
        List<XmlSchemaImport> schemaImports = new ArrayList<>();
        for (XmlSchemaObject schemaObject : schema.getItems()) {
            if (schemaObject instanceof XmlSchemaImport) {
                schemaImports.add((XmlSchemaImport) schemaObject);
            }
        }
        return schemaImports;
    }

    /**
     * Returns definition of schema element.
     */
    public static Map<QName, XmlSchemaElement> getElementsMap(XmlSchema schema) {
        return new HashMap<>(schema.getElements());
    }

    /**
     * Returns XmlSchemaElement list of elements in sequence.
     */
    public static List<XmlSchemaElement> getSequenceElements(XmlSchemaComplexContentExtension extension) {
        List<XmlSchemaElement> elements = new ArrayList<>();
        try {
            for (XmlSchemaSequenceMember sequenceMember : ((XmlSchemaSequence) extension.getParticle()).getItems()) {
                if (sequenceMember instanceof XmlSchemaElement) {
                    elements.add((XmlSchemaElement) sequenceMember);
                }
            }
            return elements;
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * Returns XmlSchemaElement list in sequence.
     */
    public static List<XmlSchemaElement> getSequenceElements(List<XmlSchemaSequenceMember> sequenceMembers) {
        List<XmlSchemaElement> elements = new ArrayList<>();
        try {
            for (XmlSchemaSequenceMember sequenceMember : sequenceMembers) {
                if (sequenceMember instanceof XmlSchemaElement) {
                    elements.add((XmlSchemaElement) sequenceMember);
                }
            }
            return elements;
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * Returns all XmlSchemaChoice element in sequence.
     */
    public static List<XmlSchemaChoice> getSequenceChoices(XmlSchemaComplexContentExtension extension) {
        List<XmlSchemaChoice> elements = new ArrayList<>();

        for (XmlSchemaSequenceMember sequenceMember : ((XmlSchemaSequence) extension.getParticle()).getItems()) {
            if (sequenceMember instanceof XmlSchemaChoice) {
                elements.add((XmlSchemaChoice) sequenceMember);
            }
        }
        return elements;
    }

    /**
     * Returns all XmlSchemaChoice elements in sequence.
     */
    public static List<XmlSchemaChoice> getSequenceChoices(List<XmlSchemaSequenceMember> sequenceMembers) {
        List<XmlSchemaChoice> elements = new ArrayList<>();

        for (XmlSchemaSequenceMember sequenceMember : sequenceMembers) {
            if (sequenceMember instanceof XmlSchemaChoice) {
                elements.add((XmlSchemaChoice) sequenceMember);
            }
        }
        return elements;
    }

    /**
     * Returns added imports.
     */
    public static List<String> getAddedImports(TreeNode<Object> actualSchemaTree, TreeNode<Object> schemaToCompareTree) {
        List<String> addedImports = new ArrayList<>();

        List<String> oldImports = TreeHelper.getSchemaImports(actualSchemaTree);
        List<String> newImports = TreeHelper.getSchemaImports(schemaToCompareTree);

        for (String importName : newImports) {
            if (!oldImports.contains(importName)) {
                addedImports.add(importName);
            }
        }
        return addedImports;
    }

    /**
     * Returns removed imports.
     */
    public static List<String> getRemovedImports(TreeNode<Object> actualSchemaTree, TreeNode<Object> schemaToCompareTree) {
        List<String> removedImports = new ArrayList<>();

        List<String> oldImports = TreeHelper.getSchemaImports(actualSchemaTree);
        List<String> newImports = TreeHelper.getSchemaImports(schemaToCompareTree);

        for (String importName : oldImports) {
            if (!newImports.contains(importName)) {
                removedImports.add(importName);
            }
        }
        return removedImports;
    }

    /**
     * Returns list of SimpleType names or ComplexType elements, which existed in both schemas.
     */
    public static List<String> getSameTypeNames(List<String> oldAfTypeNames, List<String> newAfTypeNames) {
        List<String> sameTypeElements = new ArrayList<>();

        for (String oldName : oldAfTypeNames) {
            if (newAfTypeNames.contains(oldName)) {
                sameTypeElements.add(oldName);
            }
        }
        return sameTypeElements;
    }

    /**
     * Returns list of elements with specified names. Order could be important and should be tested.
     */
    public static List<XmlSchemaElement> getElementsFromComplexTypeBySpecificNames(List<XmlSchemaElement> elements, List<String> elementsNames) {
        List<XmlSchemaElement> specificElements = new ArrayList<>();

        for (XmlSchemaElement element : elements) {
            if (elementsNames.contains(element.getName())) {
                specificElements.add(element);
            }
        }
        return specificElements;
    }

    /**
     * Returns result of comparison ofSimpleType types.
     */
    public static List<String> getDifferenceBetweenSimpleTypes(List<XmlSchemaSimpleType> oldSimpleTypes, List<XmlSchemaSimpleType> newSimpleTypes) {
        List<String> differences = new ArrayList<>();

        if (oldSimpleTypes.size() != newSimpleTypes.size()) {
            try {
                throw new Exception("ComplexType list have different size");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (int idx = 0; idx < oldSimpleTypes.size() && idx < newSimpleTypes.size(); idx++) {

            // annotations comparison
            String oldAnnotationDescription = SchemaHelper.getSimpleTypeAnnotation(oldSimpleTypes.get(idx));
            String newAnnotationDescription = SchemaHelper.getSimpleTypeAnnotation(newSimpleTypes.get(idx));

            if (oldAnnotationDescription != null && newAnnotationDescription != null && !oldAnnotationDescription.equals(newAnnotationDescription)) {
                differences.add("For SimpleType element " + newSimpleTypes.get(idx).getName() + " changed description: " + newAnnotationDescription);
            }

            // restriction id
            String oldRestrictionId = oldSimpleTypes.get(idx).getId();
            String newRestrictionId = newSimpleTypes.get(idx).getId();

            if (oldRestrictionId != null && newRestrictionId != null && !oldRestrictionId.equals(newRestrictionId)) {
                differences.add("For SimpleType element " + newSimpleTypes.get(idx).getName() + " changed id: " + newRestrictionId);
            }

            // restriction base
            String oldRestrictionBase = oldSimpleTypes.get(idx).getQName().getLocalPart();
            String newRestrictionBase = newSimpleTypes.get(idx).getQName().getLocalPart();

            if (oldRestrictionBase != null && newRestrictionBase != null && !oldRestrictionBase.equals(newRestrictionBase)) {
                differences.add("For SimpleType element " + newSimpleTypes.get(idx).getName() + " changed base value:");
            } // ignore any attributes

            // restriction facets

            List<XmlSchemaFacet> oldFacets = ((XmlSchemaSimpleTypeRestriction) oldSimpleTypes.get(idx).getContent()).getFacets();
            List<XmlSchemaFacet> newFacets = ((XmlSchemaSimpleTypeRestriction) newSimpleTypes.get(idx).getContent()).getFacets();

            // added new facets
            // removed preexisting facets
            // difference between same facets value
            List<String> facetsChanges = SchemaHelper.getDifferenceBetweenFacets(oldFacets, newFacets);

            if (facetsChanges.size() != 0) {
                for (String facetsDiffInfo : facetsChanges) {
                    differences.add("In SimpleType element " + newSimpleTypes.get(idx).getName() + " " + facetsDiffInfo);
                }
            }
        }
        return differences;
    }

    /**
     * Returns changees in Facets. Changes in elements are in count:
     * XmlSchemaEnumerationFacet, XmlSchemaMaxExclusiveFacet, XmlSchemaMaxInclusiveFacet, XmlSchemaMinExclusiveFacet,
     * XmlSchemaMinInclusiveFacet, XmlSchemaPatternFacet, XmlSchemaWhiteSpaceFacet.
     * <p>
     * And also XmlSchemaNumericFacet: XmlSchemaFractionDigitsFacet, XmlSchemaLengthFacet, XmlSchemaMaxLengthFacet,
     * XmlSchemaMinLengthFacet, XmlSchemaTotalDigitsFacet.
     */
    private static List<String> getDifferenceBetweenFacets(List<XmlSchemaFacet> oldFacets, List<XmlSchemaFacet> newFacets) {

        // list of all XmlSchemaEnumerationFacet
        List<XmlSchemaEnumerationFacet> oldEnumerationFacets = new ArrayList<>();

        for (XmlSchemaFacet facet : oldFacets) {
            if (facet instanceof XmlSchemaEnumerationFacet) {
                oldEnumerationFacets.add((XmlSchemaEnumerationFacet) facet);
            }
        }

        List<XmlSchemaEnumerationFacet> newEnumerationFacets = new ArrayList<>();
        for (XmlSchemaFacet facet : newFacets) {
            if (facet instanceof XmlSchemaEnumerationFacet) {
                newEnumerationFacets.add((XmlSchemaEnumerationFacet) facet);
            }
        }

        List<String> facetDiffs = new ArrayList<>();

        // TODO: refactoring
        // enumeration restrictions
        facetDiffs.addAll(SchemaHelper.getDifferenceBetweenEnumerationFacets(oldEnumerationFacets, newEnumerationFacets));

        // Exclusive restrictions
        facetDiffs.addAll(SchemaHelper.getDifferenceBetweenMaxExclusiveFacets(oldFacets, newFacets));
        facetDiffs.addAll(SchemaHelper.getDifferenceBetweenMinExclusiveFacets(oldFacets, newFacets));

        // Inclusive restrictions
        facetDiffs.addAll(SchemaHelper.getDifferenceBetweenMaxInclusiveFacets(oldFacets, newFacets));
        facetDiffs.addAll(SchemaHelper.getDifferenceBetweenMinInclusiveFacets(oldFacets, newFacets));

        // fractionDigits restriction
        facetDiffs.addAll(SchemaHelper.getDifferenceBetweenFractionDigitsFacets(oldFacets, newFacets));

        // length restriction
        facetDiffs.addAll(SchemaHelper.getDifferenceBetweenLengthFacets(oldFacets, newFacets));
        // maxLength restriction
        facetDiffs.addAll(SchemaHelper.getDifferenceBetweenMaxLengthFacets(oldFacets, newFacets));
        // minLength restriction
        facetDiffs.addAll(SchemaHelper.getDifferenceBetweenMinLengthFacets(oldFacets, newFacets));
        // totalDigits restriction
        facetDiffs.addAll(SchemaHelper.getDifferenceBetweenTotalDigitsFacets(oldFacets, newFacets));

        // restriction
        facetDiffs.addAll(SchemaHelper.getDifferenceBetweenPatternFacets(oldFacets, newFacets));

        // whiteSpace restriction
        facetDiffs.addAll(SchemaHelper.getDifferenceBetweenWhiteSpaceFacets(oldFacets, newFacets));

        return facetDiffs;
    }

    /**
     * Add information about chagnes in Restriction to diffs.
     */
    private static List<String> getDiffInfoFromFacets(XmlSchemaFacet oldFacet, XmlSchemaFacet newFacet, String label) {
        List<String> diffs = new ArrayList<>();

        // added new restriction
        if (oldFacet == null && newFacet != null) {
            diffs.add("added new restriction " + label + " with value: " + newFacet.getValue());
        }

        // removed old restriction
        if (newFacet == null && oldFacet != null) {
            diffs.add("removed old restriction " + label + " with value: " + oldFacet.getValue());
        }

        // value was changed
        if (oldFacet != null && newFacet != null && !oldFacet.getValue().equals(newFacet.getValue())) {
            diffs.add("restriction value was changed  " + label + " from " + oldFacet.getValue() + " to " + newFacet.getValue());
        }
        return diffs;
    }

    /**
     * Returns difference between TotalDigits in Facet.
     */
    private static List<String> getDifferenceBetweenTotalDigitsFacets(List<XmlSchemaFacet> oldFacets, List<XmlSchemaFacet> newFacets) {
        XmlSchemaTotalDigitsFacet oldTotalDigitsFacet = null;

        for (XmlSchemaFacet facet : oldFacets) {
            if (facet instanceof XmlSchemaTotalDigitsFacet) {
                oldTotalDigitsFacet = (XmlSchemaTotalDigitsFacet) facet;
            }
        }

        XmlSchemaTotalDigitsFacet newTotalDigitsFacet = null;
        for (XmlSchemaFacet facet : newFacets) {
            if (facet instanceof XmlSchemaTotalDigitsFacet) {
                newTotalDigitsFacet = (XmlSchemaTotalDigitsFacet) facet;
            }
        }
        return getDiffInfoFromFacets(oldTotalDigitsFacet, newTotalDigitsFacet, "totalDigits");
    }

    /**
     * Ищет изменения между MinLength в Facet.
     */
    private static List<String> getDifferenceBetweenMinLengthFacets(List<XmlSchemaFacet> oldFacets, List<XmlSchemaFacet> newFacets) {
        XmlSchemaMinLengthFacet oldMinLengthFacet = null;
        for (XmlSchemaFacet facet : oldFacets) {
            if (facet instanceof XmlSchemaMinLengthFacet) {
                oldMinLengthFacet = (XmlSchemaMinLengthFacet) facet;
            }
        }

        XmlSchemaMinLengthFacet newMinLengthFacet = null;
        for (XmlSchemaFacet facet : newFacets) {
            if (facet instanceof XmlSchemaMinLengthFacet) {
                newMinLengthFacet = (XmlSchemaMinLengthFacet) facet;
            }
        }
        return getDiffInfoFromFacets(oldMinLengthFacet, newMinLengthFacet, "minLength");
    }

    /**
     * Returns changed beetween MaxLength in Facet.
     */
    private static List<String> getDifferenceBetweenMaxLengthFacets(List<XmlSchemaFacet> oldFacets, List<XmlSchemaFacet> newFacets) {
        XmlSchemaMaxLengthFacet oldMaxLengthFacet = null;

        for (XmlSchemaFacet facet : oldFacets) {
            if (facet instanceof XmlSchemaMaxLengthFacet) {
                oldMaxLengthFacet = (XmlSchemaMaxLengthFacet) facet;
            }
        }

        XmlSchemaMaxLengthFacet newMaxLengthFacet = null;
        for (XmlSchemaFacet facet : newFacets) {
            if (facet instanceof XmlSchemaMaxLengthFacet) {
                newMaxLengthFacet = (XmlSchemaMaxLengthFacet) facet;
            }
        }
        return getDiffInfoFromFacets(oldMaxLengthFacet, newMaxLengthFacet, "maxLength");
    }

    /**
     * Returns changes between Length in Facet.
     */
    private static List<String> getDifferenceBetweenLengthFacets(List<XmlSchemaFacet> oldFacets, List<XmlSchemaFacet> newFacets) {
        XmlSchemaLengthFacet oldLengthFacet = null;
        for (XmlSchemaFacet facet : oldFacets) {
            if (facet instanceof XmlSchemaLengthFacet) {
                oldLengthFacet = (XmlSchemaLengthFacet) facet;
            }
        }

        XmlSchemaLengthFacet newLengthFacet = null;
        for (XmlSchemaFacet facet : newFacets) {
            if (facet instanceof XmlSchemaLengthFacet) {
                newLengthFacet = (XmlSchemaLengthFacet) facet;
            }
        }
        return getDiffInfoFromFacets(oldLengthFacet, newLengthFacet, "length");
    }

    /**
     * Returns changed between FractionDigits in Facet.
     */
    private static List<String> getDifferenceBetweenFractionDigitsFacets(List<XmlSchemaFacet> oldFacets, List<XmlSchemaFacet> newFacets) {
        XmlSchemaFractionDigitsFacet oldFractionDigitsFacet = null;
        for (XmlSchemaFacet facet : oldFacets) {
            if (facet instanceof XmlSchemaFractionDigitsFacet) {
                oldFractionDigitsFacet = (XmlSchemaFractionDigitsFacet) facet;
            }
        }

        XmlSchemaFractionDigitsFacet newFractionDigitsFacet = null;
        for (XmlSchemaFacet facet : newFacets) {
            if (facet instanceof XmlSchemaFractionDigitsFacet) {
                newFractionDigitsFacet = (XmlSchemaFractionDigitsFacet) facet;
            }
        }
        return getDiffInfoFromFacets(oldFractionDigitsFacet, newFractionDigitsFacet, "fractionDigits");
    }

    /**
     * Returns changes between WhiteSpace in Facet.
     */
    private static List<String> getDifferenceBetweenWhiteSpaceFacets(List<XmlSchemaFacet> oldFacets, List<XmlSchemaFacet> newFacets) {
        XmlSchemaWhiteSpaceFacet oldWhiteSpaceFacet = null;
        for (XmlSchemaFacet facet : oldFacets) {
            if (facet instanceof XmlSchemaWhiteSpaceFacet) {
                oldWhiteSpaceFacet = (XmlSchemaWhiteSpaceFacet) facet;
            }
        }

        XmlSchemaWhiteSpaceFacet newWhiteSpaceFacet = null;
        for (XmlSchemaFacet facet : newFacets) {
            if (facet instanceof XmlSchemaWhiteSpaceFacet) {
                newWhiteSpaceFacet = (XmlSchemaWhiteSpaceFacet) facet;
            }
        }
        return getDiffInfoFromFacets(oldWhiteSpaceFacet, newWhiteSpaceFacet, "whiteSpace");
    }

    /**
     * Returns changes between Pattern in Facet.
     */
    private static List<String> getDifferenceBetweenPatternFacets(List<XmlSchemaFacet> oldFacets, List<XmlSchemaFacet> newFacets) {
        XmlSchemaPatternFacet oldPatternFacet = null;
        for (XmlSchemaFacet facet : oldFacets) {
            if (facet instanceof XmlSchemaPatternFacet) {
                oldPatternFacet = (XmlSchemaPatternFacet) facet;
            }
        }

        XmlSchemaPatternFacet newPatternFacet = null;
        for (XmlSchemaFacet facet : newFacets) {
            if (facet instanceof XmlSchemaPatternFacet) {
                newPatternFacet = (XmlSchemaPatternFacet) facet;
            }
        }
        return getDiffInfoFromFacets(oldPatternFacet, newPatternFacet, "pattern");
    }

    /**
     * Returns changes between MinInclusive in Facet.
     */
    private static List<String> getDifferenceBetweenMinInclusiveFacets(List<XmlSchemaFacet> oldFacets, List<XmlSchemaFacet> newFacets) {
        XmlSchemaMinInclusiveFacet oldMinInclusiveFacet = null;
        for (XmlSchemaFacet facet : oldFacets) {
            if (facet instanceof XmlSchemaMinInclusiveFacet) {
                oldMinInclusiveFacet = (XmlSchemaMinInclusiveFacet) facet;
            }
        }

        XmlSchemaMinInclusiveFacet newMinInclusiveFacet = null;
        for (XmlSchemaFacet facet : newFacets) {
            if (facet instanceof XmlSchemaMinInclusiveFacet) {
                newMinInclusiveFacet = (XmlSchemaMinInclusiveFacet) facet;
            }
        }
        return getDiffInfoFromFacets(oldMinInclusiveFacet, newMinInclusiveFacet, "minInclusive");
    }

    /**
     * Returns changes between MaxInclusive in Facets.
     */
    private static List<String> getDifferenceBetweenMaxInclusiveFacets(List<XmlSchemaFacet> oldFacets, List<XmlSchemaFacet> newFacets) {
        XmlSchemaMaxInclusiveFacet oldMaxInclusiveFacet = null;
        for (XmlSchemaFacet facet : oldFacets) {
            if (facet instanceof XmlSchemaMaxInclusiveFacet) {
                oldMaxInclusiveFacet = (XmlSchemaMaxInclusiveFacet) facet;
            }
        }

        XmlSchemaMaxInclusiveFacet newMaxInclusiveFacet = null;
        for (XmlSchemaFacet facet : newFacets) {
            if (facet instanceof XmlSchemaMaxInclusiveFacet) {
                newMaxInclusiveFacet = (XmlSchemaMaxInclusiveFacet) facet;
            }
        }
        return getDiffInfoFromFacets(oldMaxInclusiveFacet, newMaxInclusiveFacet, "maxInclusive");
    }

    /**
     * Returns changes between MinExclusive in Facets.
     */
    private static List<String> getDifferenceBetweenMinExclusiveFacets(List<XmlSchemaFacet> oldFacets, List<XmlSchemaFacet> newFacets) {
        XmlSchemaMinExclusiveFacet oldMinExclusiveFacet = null;
        for (XmlSchemaFacet facet : oldFacets) {
            if (facet instanceof XmlSchemaMinExclusiveFacet) {
                oldMinExclusiveFacet = (XmlSchemaMinExclusiveFacet) facet;
            }
        }

        XmlSchemaMinExclusiveFacet newMinExclusiveFacet = null;
        for (XmlSchemaFacet facet : newFacets) {
            if (facet instanceof XmlSchemaMinExclusiveFacet) {
                newMinExclusiveFacet = (XmlSchemaMinExclusiveFacet) facet;
            }
        }
        return getDiffInfoFromFacets(oldMinExclusiveFacet, newMinExclusiveFacet, "minExclusive");
    }

    /**
     * Returns changes between MaxExclusive in Facets.
     */
    private static List<String> getDifferenceBetweenMaxExclusiveFacets(List<XmlSchemaFacet> oldFacets, List<XmlSchemaFacet> newFacets) {
        XmlSchemaMaxExclusiveFacet oldMaxExclusiveFacet = null;
        for (XmlSchemaFacet facet : oldFacets) {
            if (facet instanceof XmlSchemaMaxExclusiveFacet) {
                oldMaxExclusiveFacet = (XmlSchemaMaxExclusiveFacet) facet;
            }
        }

        XmlSchemaMaxExclusiveFacet newMaxExclusiveFacet = null;
        for (XmlSchemaFacet facet : newFacets) {
            if (facet instanceof XmlSchemaMaxExclusiveFacet) {
                newMaxExclusiveFacet = (XmlSchemaMaxExclusiveFacet) facet;
            }
        }
        return getDiffInfoFromFacets(oldMaxExclusiveFacet, newMaxExclusiveFacet, "maxExclusive");
    }

    /**
     * Returns changes between enumeration in Facets.
     */
    private static List<String> getDifferenceBetweenEnumerationFacets(List<XmlSchemaEnumerationFacet> oldEnumerationFacets, List<XmlSchemaEnumerationFacet> newEnumerationFacets) {
        List<String> diffs = new ArrayList<>();

        List<String> oldEnumerationValues = new ArrayList<>();
        for (XmlSchemaEnumerationFacet facet : oldEnumerationFacets) {
            oldEnumerationValues.add((String) facet.getValue());
        }

        List<String> newEnumerationValues = new ArrayList<>();
        for (XmlSchemaEnumerationFacet facet : newEnumerationFacets) {
            newEnumerationValues.add((String) facet.getValue());
        }

        // added new enumeration
        List<String> getNewEnumerationValues = SchemaHelper.getNewEnumerationValues(oldEnumerationValues, newEnumerationValues);

        // removed old enumeration
        List<String> getPreExistingEnumerationValues = SchemaHelper.getPreExistingEnumerationValues(oldEnumerationValues, newEnumerationValues);

        diffs.addAll(getNewEnumerationValues);
        diffs.addAll(getPreExistingEnumerationValues);

        return diffs;
    }

    /**
     * Returns new Enumeration values.
     */
    private static List<String> getNewEnumerationValues(List<String> oldEnumerationValues, List<String> newEnumerationValues) {
        List<String> addedEnumerations = new ArrayList<>();
        for (String value : newEnumerationValues) {
            if (!oldEnumerationValues.contains(value)) {
                addedEnumerations.add("added new Enumeration element with value: " + value);
            }
        }
        return addedEnumerations;
    }

    /**
     * Returns old Enumeration values.
     */
    private static List<String> getPreExistingEnumerationValues(List<String> oldEnumerationValues, List<String> newEnumerationValues) {
        List<String> removedEnumerations = new ArrayList<>();
        for (String value : oldEnumerationValues) {
            if (!newEnumerationValues.contains(value)) {
                removedEnumerations.add("removed existed Enumeration with value: " + value);
            }
        }
        return removedEnumerations;
    }
}
