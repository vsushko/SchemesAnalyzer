package ru.vsushko.analyzer.schema;

import com.sun.org.apache.xerces.internal.dom.DeferredTextImpl;
import org.apache.ws.commons.schema.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import ru.vsushko.analyzer.schema.complextype.sequence.ElementImpl;
import ru.vsushko.analyzer.schema.complextype.attribute.Attribute;
import ru.vsushko.analyzer.schema.complextype.attribute.AttributeImpl;
import ru.vsushko.analyzer.schema.complextype.sequence.Element;
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
import java.util.*;
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
     * Возвращает схему по указанному пути.
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
     * Выдергивает через XPath первое вхождение
     * <xs:documentation><xs:annotation>Описание схемы</xs:annotation></xs:documentation>.
     */
    public static String getSchemaDescription(String pathToSchema) {
        String schemaDescription;
        try {
            FileInputStream file = new FileInputStream(new File(pathToSchema));
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder =  builderFactory.newDocumentBuilder();
            Document xmlDocument = builder.parse(file);
            XPath xPath =  XPathFactory.newInstance().newXPath();

            String expression = "//annotation/documentation";
            Node node = (Node) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODE);
            schemaDescription = ((DeferredTextImpl) node.getFirstChild()).getData();
            return schemaDescription;

        } catch (ParserConfigurationException e) {
            System.out.println("Не удалось создать фабрику");
        } catch (XPathExpressionException e) {
            System.out.println("Не удалось распознать XPath выражение");
        } catch (FileNotFoundException e) {
            System.out.println("Не удалось найти файл " + pathToSchema);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Возвращает версию схемы.
     */
    public static String readSchemaVersionInfo(String schemaTargetNamespace) {
        Pattern pattern = Pattern.compile("\\d.{2,}");
        Matcher matcher = pattern.matcher(schemaTargetNamespace);
        return matcher.find() ? matcher.group() : null;
    }

    /**
     * Возвращает список ComplexType.
     */
    public static List<String> getComplexTypeListFromSchema(XmlSchema xmlSchema) {
        List<String> complexTypeNames = new ArrayList<String>();

        // Положим в список имена всех ComplexType
        for (QName type : new ArrayList<QName>(xmlSchema.getSchemaTypes().keySet())) {
            if (xmlSchema.getSchemaTypes().get(type) instanceof XmlSchemaComplexType) {
                complexTypeNames.add(type.getLocalPart());
            }
        }
        return complexTypeNames;
    }

    /**
     * Возвращает список SimpleType.
     */
    public static List<String> getSimpleTypeListFromSchema(XmlSchema xmlSchema) {
        List<String> simpleTypeNames = new ArrayList<String>();

        // Положим в список имена всех SimpleType
        for (QName type : new ArrayList<QName>(xmlSchema.getSchemaTypes().keySet())) {
            if (xmlSchema.getSchemaTypes().get(type) instanceof XmlSchemaSimpleType) {
                simpleTypeNames.add(type.getLocalPart());
            }
        }
        return simpleTypeNames;
    }

    /**
     * Ищет новые ComplexType.
     */
    public static List<String> getNewComplexTypes(List<String> actualTypeNames, List<String> typeNamesToCompare) {
        List<String> newComplexTypeNames = new ArrayList<String>();
        newComplexTypeNames.addAll(typeNamesToCompare);

        for (String typeName : actualTypeNames) {
            newComplexTypeNames.remove(typeName);
        }
        return newComplexTypeNames;
    }

    /**
     * Возвращает список всех ComplexType существовавших ранее.
     */
    public static List<String> getPreExistingComplexTypes(List<String> actualTypeNames, List<String> typeNamesToCompare) {
        List<String> preExistingTypeNames = new ArrayList<String>();
        preExistingTypeNames.addAll(actualTypeNames);

        for (String typeName : typeNamesToCompare) {
            preExistingTypeNames.remove(typeName);
        }
        return preExistingTypeNames;
    }

    /**
     * Ищет новые SimpleType.
     */
    public static List<String> getNewSimpleTypes(List<String> actualTypeNames, List<String> typeNamesToCompare) {
        List<String> newSimpleTypeNames = new ArrayList<String>();
        newSimpleTypeNames.addAll(typeNamesToCompare);

        for (String typeName : actualTypeNames) {
            newSimpleTypeNames.remove(typeName);
        }
        return newSimpleTypeNames;
    }

    /**
     * Возвращает список всех SimpleType существовавших ранее.
     */
    public static List<String> getPreExistingSimpleTypes(List<String> actualTypeNames, List<String> typeNamesToCompare) {
        List<String> preExistingElements = new ArrayList<String>();
        preExistingElements.addAll(actualTypeNames);

        for (String typeName : typeNamesToCompare) {
            preExistingElements.remove(typeName);
        }
        return preExistingElements;
    }

    /**
     * Возвращает изменения между ComplexType элементами.
     */
    public static List<String> getDifferenceBetweenComplexTypes(List<XmlSchemaComplexType> oldComplexTypes,
                                                                List<XmlSchemaComplexType> newComplexTypes) {
        List<String> differences = new ArrayList<String>();

        if (oldComplexTypes.size() != newComplexTypes.size()) {
            try {
                throw new Exception("Списки для сравнения элементов у комплексных типов не совпадают по длине");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (int idx = 0; idx < oldComplexTypes.size() && idx < newComplexTypes.size(); idx++) {
            String complexTypeName = newComplexTypes.get(idx).getName();

            // сравним аннотации
            String oldAnnotationDescription = SchemaHelper.getComplexTypeAnnotation(oldComplexTypes.get(idx));
            String newAnnotationDescription = SchemaHelper.getComplexTypeAnnotation(newComplexTypes.get(idx));

            // аннотаций может не быть
            if (oldAnnotationDescription != null && newAnnotationDescription != null
                    && !oldAnnotationDescription.equals(newAnnotationDescription)){
                differences.add("\n" + newComplexTypes.get(idx).getName() + " изменено описание: " + newAnnotationDescription + "\n");
            }

            // случай, когда нужно брать данные из ComplexContent (глобальные прикладные типы)
            if (newComplexTypes.get(idx).getContentModel() != null && oldComplexTypes.get(idx).getContentModel() != null
                    && newComplexTypes.get(idx).getParticle() == null && oldComplexTypes.get(idx).getParticle() == null) {

                XmlSchemaComplexContentExtension oldExtension =
                        (XmlSchemaComplexContentExtension) oldComplexTypes.get(idx).getContentModel().getContent();
                XmlSchemaComplexContentExtension newExtension =
                        (XmlSchemaComplexContentExtension) newComplexTypes.get(idx).getContentModel().getContent();

                // сравниваем элементы последовательности
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
            // случай, когда нужно брать данные из Particle (локальные прикладные типы)
            if (newComplexTypes.get(idx).getParticle() != null && oldComplexTypes.get(idx).getParticle() != null
                    && newComplexTypes.get(idx).getContentModel() == null && oldComplexTypes.get(idx).getContentModel() == null) {

                XmlSchemaSequence oldExtension = (XmlSchemaSequence) oldComplexTypes.get(idx).getParticle();
                XmlSchemaSequence newExtension = (XmlSchemaSequence) newComplexTypes.get(idx).getParticle();

                // сравниваем элементы последовательности
                List<String> sequenceChanges = SchemaHelper.getDifferenceBetweenExtensionSequencesElements(oldExtension.getItems(), newExtension.getItems());

                if (sequenceChanges.size() != 0) {
                    if (!differences.contains(complexTypeName)) {
                        differences.add(complexTypeName + "\n");
                    }
                    for (String diff : sequenceChanges) {
                        differences.add(" " + diff + "\n");
                    }
                }

                List<XmlSchemaAttributeOrGroupRef> oldAttributes =  oldComplexTypes.get(idx).getAttributes();
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
     * Возвращает изменения в среди Elements и Choice в XmlSchemaComplexContentExtension.
     */
    private static List<String> getDifferenceBetweenExtensionSequencesElements(XmlSchemaComplexContentExtension oldExtension,
                                                                               XmlSchemaComplexContentExtension newExtension) {
        List<String> difference = new ArrayList<String>();

        List<XmlSchemaElement> oldElements = SchemaHelper.getSequenceElements(oldExtension);
        List<XmlSchemaElement> newElements = SchemaHelper.getSequenceElements(newExtension);

        // extension может и не быть, элементов тоже
        if (oldElements != null && newElements != null) {
            // добавлены новые XmlSchemaElement элементы
            difference.addAll(SchemaHelper.getNewElementsInComplexTypeSequenceElements(oldElements, newElements));
            // удалены ранее объявленные XmlSchemaElement элементы
            difference.addAll(SchemaHelper.getRemovedElementsInComplexTypeSequenceElements(oldElements, newElements));

            // получим список имен элементов старой схемы
            List<String> oldElementsNames = SchemaHelper.getElementsNames(oldElements);

            // получим список имен элементов новой схемы
            List<String> newElementsNames = SchemaHelper.getElementsNames(newElements);

            // получим список XmlSchemaElement элементов с одинаковыми именами
            List<String> sameSchemaElementsNames = SchemaHelper.getSameTypeNames(oldElementsNames, newElementsNames);

            List<XmlSchemaElement> oldSameElements = SchemaHelper.getElementsFromComplexTypeBySpecificNames(oldElements, sameSchemaElementsNames);
            List<XmlSchemaElement> newSameElements = SchemaHelper.getElementsFromComplexTypeBySpecificNames(newElements, sameSchemaElementsNames);

            // получим список изменений между параметрами у элементов
            difference.addAll(SchemaHelper.getDifferenceBetweenExtensionSequencesElementsValues(oldSameElements, newSameElements));

            // если есть choice, их тоже анализируем
            List<XmlSchemaChoice> oldChoices = SchemaHelper.getSequenceChoices(oldExtension);
            List<XmlSchemaChoice> newChoices = SchemaHelper.getSequenceChoices(newExtension);

            if (oldChoices.size() != 0 && oldChoices.size() != 0) {
                difference.addAll(SchemaHelper.getDifferenceBetweenChoices(oldChoices, newChoices));
            }

            // удален ранее объявленный XmlSchemaChoice
            if (oldChoices.size() != 0 && newChoices.size() == 0) {
                // достать описание
                difference.add("удален ранее объявленный choice элемент");
            }

            // добавлен новый XmlSchemaChoice
            if (oldChoices.size() == 0 && newChoices.size() != 0) {
                // достать описание
                difference.add("добавлен новый choice элемент");
            }
        }
        return difference;
    }

    private static List<String> getDifferenceBetweenChoices(List<XmlSchemaChoice> oldChoices, List<XmlSchemaChoice> newChoices) {
        List<String> difference = new ArrayList<String>();

        String newDescription;
        // добавлено описание
        if (oldChoices.size() == 0 && newChoices.size() == 1) {
            for (XmlSchemaChoice choice : newChoices) {
                newDescription = SchemaHelper.getChoiceAnnotation(choice);
                if (newDescription != null) {
                    difference.add("у choice элемента добавлено новое описание " + newDescription);
                }
            }
        }
        String oldDescription;
        // удалено описание
        if (oldChoices.size() == 1 && newChoices.size() == 0) {
            for (XmlSchemaChoice choice : oldChoices) {
                 oldDescription = SchemaHelper.getChoiceAnnotation(choice);
                if (oldDescription != null) {
                    difference.add("у choice элемента удалено ранее объявленное описание " + oldDescription);
                }
            }
        }
        // если choice элементы присутствуют в обеих схемах, то сравниваем элементы
        if (oldChoices.size() == 1 && newChoices.size() == 1) {
            // сравним описания
            newDescription = SchemaHelper.getChoiceAnnotation(newChoices.get(0));
            oldDescription = SchemaHelper.getChoiceAnnotation(oldChoices.get(0));

            if (newDescription != null && oldDescription != null &&
                    !oldDescription.equals(newDescription)) {
                difference.add("у choice элемента изменено описание : \n" + newDescription);
            }

            // id=ID
            String oldId = oldChoices.get(0).getId();
            String newId = newChoices.get(0).getId();
            if (oldId != null && newId != null && !oldId.equals(newId)) {
                difference.add("у choice элемента изменилось значение атрибута 'id ' c " + oldId + " на " + newId);
            }

            // TODO: fix choice difference output

            // maxOccurs=nonNegativeInteger|unbounded
            String oldMaxOccurs = String.valueOf(oldChoices.get(0).getMaxOccurs());
            String newMaxOccurs = String.valueOf(newChoices.get(0).getMaxOccurs());
            if (!oldMaxOccurs.equals(newMaxOccurs)) {
                difference.add("у choice элемента изменилось значение атрибута 'maxOccurs ' c " + oldMaxOccurs + " на " + newMaxOccurs);
            }

            // minOccurs=nonNegativeInteger
            String oldMinOccurs = String.valueOf(oldChoices.get(0).getMinOccurs());
            String newMinOccurs = String.valueOf(newChoices.get(0).getMinOccurs());
            if (!oldMinOccurs.equals(newMinOccurs)) {
                difference.add("у choice элемента изменилось значение атрибута 'minOccurs ' c " + oldMinOccurs + " на " + newMinOccurs);
            }

            List<XmlSchemaElement> oldChoiceElements = SchemaHelper.getChoicesElements(oldChoices);
            List<XmlSchemaElement> newChoiceElements = SchemaHelper.getChoicesElements(newChoices);

            // добавлены новые XmlSchemaElement элементы
            difference.addAll(SchemaHelper.getNewElementsInComplexTypeSequenceElements(oldChoiceElements, newChoiceElements));
            // удалены ранее объявленные XmlSchemaElement элементы
            difference.addAll(SchemaHelper.getRemovedElementsInComplexTypeSequenceElements(oldChoiceElements, newChoiceElements));

            // получим список имен элементов старых choice
            List<String> oldElementsNames = SchemaHelper.getElementsNames(oldChoiceElements);

            // получим список имен элементов новый choice
            List<String> newElementsNames = SchemaHelper.getElementsNames(newChoiceElements);

            // получим список XmlSchemaChoice элементов с одинаковыми именами
            List<String> sameSchemaElementsNames = SchemaHelper.getSameTypeNames(oldElementsNames, newElementsNames);

            List<XmlSchemaElement> oldSameElements = SchemaHelper.getElementsFromComplexTypeBySpecificNames(oldChoiceElements, sameSchemaElementsNames);
            List<XmlSchemaElement> newSameElements = SchemaHelper.getElementsFromComplexTypeBySpecificNames(newChoiceElements, sameSchemaElementsNames);

            difference.addAll(SchemaHelper.getDifferenceBetweenExtensionSequencesElementsValues(oldSameElements, newSameElements));
        }
        return difference;
    }

    /**
     * Возвращает список элементов из Choice.
     */
    private static List<XmlSchemaElement> getChoicesElements(List<XmlSchemaChoice> choices) {
        List<XmlSchemaElement> elements = new ArrayList<XmlSchemaElement>();

        // работаем только с первыми choice элементами
        if (choices.size() == 1) {
            for (XmlSchemaChoice choice : choices) {
                for (XmlSchemaChoiceMember member : choice.getItems()) {
                    // и только с XmlSchemaElements внутри, можно реализовать с sequence
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
     * Возвращает изменения между ComplexType элементами, работает с XmlSchemaSequenceMember.
     */
    private static List<String> getDifferenceBetweenExtensionSequencesElements(List<XmlSchemaSequenceMember> oldMemberList,
                                                                               List<XmlSchemaSequenceMember> newMemberList) {
        List<String> difference = new ArrayList<String>();

        List<XmlSchemaElement> oldElements = SchemaHelper.getSequenceElements(oldMemberList);
        List<XmlSchemaElement> newElements = SchemaHelper.getSequenceElements(newMemberList);

        // extension может и не быть, элементов тоже
        if (oldElements != null && newElements != null) {
            // добавлены новые XmlSchemaElement элементы
            difference.addAll(SchemaHelper.getNewElementsInComplexTypeSequenceElements(oldElements, newElements));
            // удалены ранее объявленные XmlSchemaElement элементы
            difference.addAll(SchemaHelper.getRemovedElementsInComplexTypeSequenceElements(oldElements, newElements));

            // получим список имен элементов старой схемы
            List<String> oldElementsNames = SchemaHelper.getElementsNames(oldElements);

            // получим список имен элементов новой схемы
            List<String> newElementsNames = SchemaHelper.getElementsNames(newElements);

            // получим список XmlSchemaElement элементов с одинаковыми именами
            List<String> sameSchemaElementsNames = SchemaHelper.getSameTypeNames(oldElementsNames, newElementsNames);

            List<XmlSchemaElement> oldSameElements = SchemaHelper.getElementsFromComplexTypeBySpecificNames(oldElements, sameSchemaElementsNames);
            List<XmlSchemaElement> newSameElements = SchemaHelper.getElementsFromComplexTypeBySpecificNames(newElements, sameSchemaElementsNames);

            difference.addAll(SchemaHelper.getDifferenceBetweenExtensionSequencesElementsValues(oldSameElements, newSameElements));

            // если есть choice, их тоже анализируем
            List<XmlSchemaChoice> oldChoices = SchemaHelper.getSequenceChoices(oldMemberList);
            List<XmlSchemaChoice> newChoices = SchemaHelper.getSequenceChoices(newMemberList);

            if (oldChoices.size() != 0 && oldChoices.size() != 0) {
                difference.addAll(SchemaHelper.getDifferenceBetweenChoices(oldChoices, newChoices));
            }

            // удален ранее объявленный XmlSchemaChoice
            if (oldChoices.size() != 0 && newChoices.size() == 0) {
                // достать описание
                difference.add("удален ранее объявленный choice элемент");
            }

            // добавлен новый XmlSchemaChoice
            if (oldChoices.size() == 0 && newChoices.size() != 0) {
                // достать описание
                difference.add("добавлен новый choice элемент");
            }
        }

        return difference;
    }

    /**
     * Возвращает XmlSchemaElement по заданному имени из коллекции.
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
     * Возвращает изменения между элементами.
     */
    private static List<String> getDifferenceBetweenExtensionSequencesElementsValues(List<XmlSchemaElement> oldSameElements, List<XmlSchemaElement> newSameElements) {
        List<String> diffStrings = new ArrayList<String>();

        Element element = new ElementImpl();

        if (oldSameElements.size() == newSameElements.size()) {
            for (XmlSchemaElement newSameElement : newSameElements) {
                // получим из старой схемы элемент с таким же именем
                XmlSchemaElement oldSameElement = getXmlSchemaElementWithSameName(oldSameElements, element.getName(newSameElement));

                // сравниваем атрибуты элементов
                if (newSameElement != null && oldSameElement != null) {

                    // Type
                    if (!element.getType(oldSameElement).equals(element.getType(newSameElement))) {
                        diffStrings.add("Для элемента " + element.getName(newSameElement) + " изменен тип на " + element.getType(newSameElement));
                    }

                    /*if (!element.getPrefix(oldSameElement).equals(element.getPrefix(newSameElement))) {
                        diffStrings.add("изменилось значение атрибута 'prefix' с " + element.getPrefix(oldSameElement) + " на " + element.getPrefix(newSameElement));
                    }*/

                    // TODO: work with unbounded value
                    // Если изменено значение атрибутов minOccurs и maxOccurs одновременно
                    if (!element.getMinOccurs(oldSameElement).equals(element.getMinOccurs(newSameElement))
                            && !element.getMaxOccurs(oldSameElement).equals(element.getMaxOccurs(newSameElement))) {
                        // Если m=M, то выводим только [M]
                        if (element.getMinOccurs(newSameElement).equals(element.getMaxOccurs(newSameElement))) {
                            diffStrings.add("Для элемента " + element.getName(newSameElement) +
                                    " изменена множественность [" + element.getMaxOccurs(newSameElement) + "]");
                        } else {
                            diffStrings.add("Для элемента " + element.getName(newSameElement) +
                                    " изменена множественность [" + element.getMinOccurs(newSameElement) + ".." + element.getMaxOccurs(newSameElement) + "]");
                        }
                    } else {
                        // раньше MinOccurs было 1, а теперь другое значение
                        if (element.getMinOccurs(oldSameElement).equals("1") && !element.getMinOccurs(oldSameElement).equals(element.getMinOccurs(newSameElement))) {
                            // Если m=M=1, то [1]
                            if (element.getMinOccurs(newSameElement).equals("1") && element.getMaxOccurs(newSameElement).equals("1")) {
                                diffStrings.add("Для элемента " + element.getName(newSameElement) +
                                        " изменена обязательность [1]");
                            } else {
                                diffStrings.add("Для элемента " + element.getName(newSameElement) +
                                        " изменена обязательность [m.." + element.getMaxOccurs(newSameElement) + "]");
                            }
                        }

                        // раньше MaxOccurs было 1, а теперь другое значение
                        if (element.getMaxOccurs(oldSameElement).equals("1") && !element.getMaxOccurs(oldSameElement).equals(element.getMaxOccurs(newSameElement))) {
                            // Если m=M=1, то [1]
                            if (element.getMinOccurs(newSameElement).equals("1") && element.getMaxOccurs(newSameElement).equals("1")) {
                                diffStrings.add("Для элемента " + element.getName(newSameElement) +
                                        " изменена множественность [1]");
                            } else {
                                diffStrings.add("Для элемента " + element.getName(newSameElement) +
                                        " изменена множественность [" + element.getMinOccurs(newSameElement) + "..M]");
                            }
                        }

                        // раньше MinOccurs был объявлен, а теперь 1
                        if (element.getMinOccurs(newSameElement).equals("1") && !element.getMinOccurs(oldSameElement).equals(element.getMinOccurs(newSameElement))) {
                            // Если M=1, то [1]
                            if (element.getMaxOccurs(newSameElement).equals("1")) {
                                diffStrings.add("Для элемента " + element.getName(newSameElement) +
                                        " изменена обязательность [1]");
                            } else {
                                // MaxOccurs может быть "0"
                                diffStrings.add("Для элемента " + element.getName(newSameElement) +
                                        " изменена обязательность [1.." + element.getMaxOccurs(newSameElement) + "]");
                            }
                        }

                        // раньше MaxOccurs был объявлен, а теперь 1
                        if (element.getMaxOccurs(newSameElement).equals("1") && !element.getMaxOccurs(oldSameElement).equals(element.getMaxOccurs(newSameElement))) {
                            // если m=1, то [1]
                            if (element.getMinOccurs(newSameElement).equals("1")) {
                                diffStrings.add("Для элемента " + element.getName(newSameElement) +
                                        " изменена множественность [1]");
                            } else {
                                diffStrings.add("Для элемента " + element.getName(newSameElement) +
                                        " изменена множественность [" + element.getMinOccurs(newSameElement) + "..1]");
                            }
                        }
                    }

                    // %-)

                    // описание элемента может отсутствовать
                    String oldDescription = element.getDescription(oldSameElement);
                    String newDescription = element.getDescription(newSameElement);
                    if (oldDescription != null && newDescription != null && !oldDescription.equals(element.getDescription(newSameElement))) {
                        diffStrings.add(" Для элемента " + element.getName(newSameElement) + " изменено описание: " + element.getDescription(newSameElement));
                    }
                }
            }
        }
        return diffStrings;
    }

    private static List<String> getElementsNames(List<XmlSchemaElement> elements) {
        List<String> elementsNames = new ArrayList<String>();

        for (XmlSchemaElement element : elements) {
            elementsNames.add(element.getName());
        }
        return elementsNames;
    }

    /**
     * Возвращает изменения между атрибутами в XmlSchemaComplexContentExtension.
     */
    private static List<String> getDifferenceBetweenExtensionAttributesValues(List<XmlSchemaAttributeOrGroupRef> oldAttributes,
                                                                              List<XmlSchemaAttributeOrGroupRef> newAttributes) {
        List<String> diffStrings = new ArrayList<String>();

        // TODO: рефакторинг сравнений на null

        // если размер коллекций элементов совпадает
        if (oldAttributes.size() == newAttributes.size()) {
            for (int idx = 0; idx < oldAttributes.size() && idx < newAttributes.size(); idx++) {

                // сравниваем атрибуты
                Attribute attribute = new AttributeImpl();

                XmlSchemaAttribute oldAttribute = (XmlSchemaAttribute) oldAttributes.get(idx);
                XmlSchemaAttribute newAttribute = (XmlSchemaAttribute) newAttributes.get(idx);

                if (oldAttribute != null && newAttribute != null) {

                    String oldFixedValue = attribute.getFixedValue(oldAttribute);
                    String newFixedValue = attribute.getFixedValue(newAttribute);

                    if (oldFixedValue != null && newFixedValue != null && !oldFixedValue.equals(newFixedValue)) {
                        diffStrings.add("изменилось значение 'fixed' у атрибута " + attribute.getName(newAttribute)
                                + " с " + oldFixedValue +  " на " + newFixedValue);
                    }

                    String oldName = attribute.getName(oldAttribute);
                    String newName = attribute.getName(newAttribute);

                    if (oldName != null && newName != null && !oldName.equals(newName)) {
                        diffStrings.add("изменилось значение 'имя' у атрибута c" + oldName + " на " + newName);
                    }

                    String oldPrefix = attribute.getPrefix(oldAttribute);
                    String newPrefix = attribute.getPrefix(newAttribute);

                    if (oldPrefix != null && newPrefix != null && !oldPrefix.equals(newPrefix)) {
                        diffStrings.add("изменилось значение 'префикс' у атрибута " + newName
                                + " c " + oldPrefix + " на " + newPrefix);
                    }

                    String oldType = attribute.getType(oldAttribute);
                    String newType = attribute.getType(newAttribute);

                    if (oldType != null && newType != null && !oldType.equals(newType)) {
                        diffStrings.add("изменилось значение 'тип' у атрибута " + attribute.getName(newAttribute)
                                + " c " + oldType + " на " + newType);
                    }

                    String oldRequiredValue = attribute.getRequiredValue(oldAttribute);
                    String newRequiredValue = attribute.getRequiredValue(newAttribute);

                    if (!oldRequiredValue.equals(newRequiredValue)) {
                        diffStrings.add("изменилось значение 'use' у атрибута " + attribute.getName(newAttribute)
                                + " c " + oldRequiredValue + " на " + newRequiredValue);
                    }

                    String oldDescription = attribute.getDescription(oldAttribute);
                    String newDescription = attribute.getDescription(newAttribute);

                    if (oldDescription != null && newDescription != null && !oldDescription.equals(newDescription)) {
                        diffStrings.add("изменилось значение 'описание' у атрибута " + attribute.getName(newAttribute)
                                + ": " + attribute.getDescription(newAttribute));
                    }
                }
            }
        }
        return diffStrings;
    }

    /**
     * Возвращает список ComplexType.
     */
    public static List<XmlSchemaComplexType> getComplexTypeItems(XmlSchema xmlSchema) {
        List<XmlSchemaComplexType> schemaComplexTypes = new ArrayList<XmlSchemaComplexType>();

        for (XmlSchemaObject schemaObject : xmlSchema.getItems()) {
            if (schemaObject instanceof XmlSchemaComplexType) {
                schemaComplexTypes.add((XmlSchemaComplexType) schemaObject);
            }
        }
        return schemaComplexTypes;
    }

    /**
     * Возвращает список SimpleType.
     */
    public static List<XmlSchemaSimpleType> getSimpleTypeItems(XmlSchema xmlSchema) {
        List<XmlSchemaSimpleType> schemaSimpleTypes = new ArrayList<XmlSchemaSimpleType>();

        for (XmlSchemaObject schemaObject : xmlSchema.getItems()) {
            if (schemaObject instanceof XmlSchemaSimpleType) {
                schemaSimpleTypes.add((XmlSchemaSimpleType) schemaObject);
            }
        }
        return schemaSimpleTypes;
    }

    /**
     * Возвращает новые элементы последовательности.
     */
    private static List<String> getNewElementsInComplexTypeSequenceElements(List<XmlSchemaElement> oldElements, List<XmlSchemaElement> newElements) {
        List<String> diffs = new ArrayList<String>();

        // получим список всех имен старых элементов
        List<String> oldElementNames = new ArrayList<String>();
        for (XmlSchemaElement schemaElement : oldElements) {
            oldElementNames.add(schemaElement.getName());
        }

        // получим список имен новых элементов
        List<String> newElementNames = new ArrayList<String>();
        for (XmlSchemaElement schemaElement : newElements) {
            newElementNames.add(schemaElement.getName());
        }

        List<String> addedElements = SchemaHelper.getAddedElements(oldElementNames, newElementNames);

        for (XmlSchemaElement element : newElements) {
            if (addedElements.contains(element.getName())) {
                diffs.add("Добавлен элемент " + element.getName() + "\n Описание: " + getElementTypeAnnotation(element));
            }
        }
        return diffs;
    }

    /**
     * Возвращает список добавленных элементов.
     */
    public static List<String> getAddedElements(List<String> oldElementsNames, List<String> newElementsNames) {
        List<String> addedElements = new ArrayList<String>();

        // перебираем новые элементы
        for (String elementName : newElementsNames) {
            // если новый элемент не найден в старом списке, то значит он добавлен
            if (!oldElementsNames.contains(elementName)) {
                addedElements.add(elementName);
            }
        }
        return addedElements;
    }


    /**
     * Возвращает удаленые элементы последовательности.
     */
    private static List<String> getRemovedElementsInComplexTypeSequenceElements(List<XmlSchemaElement> oldElements, List<XmlSchemaElement> newElements) {
        List<String> diffs = new ArrayList<String>();

        // получим список всех имен старых элементов
        List<String> oldElementNames = new ArrayList<String>();
        for (XmlSchemaElement schemaElement : oldElements) {
            oldElementNames.add(schemaElement.getName());
        }

        // получим список имен новых элементов
        List<String> newElementNames = new ArrayList<String>();
        for (XmlSchemaElement schemaElement : newElements) {
            newElementNames.add(schemaElement.getName());
        }

        List<String> removedElements = SchemaHelper.getRemovedElements(oldElementNames, newElementNames);

        for (XmlSchemaElement element : oldElements) {
            if (removedElements.contains(element.getName())) {
                diffs.add("Удален элемент " + element.getName() + "\n Описание: " + getElementTypeAnnotation(element));
            }
        }
        return diffs;
    }

    /**
     * Возвращает список удаленных элементов.
     */
    public static List<String> getRemovedElements(List<String> oldElementsNames, List<String> newElementsNames) {
        List<String> removedElements = new ArrayList<String>();

        // перебираем старые элементы
        for (String elementName : oldElementsNames) {
            // если старый элемент не найден в новом списке, то значит он удален
            if (!newElementsNames.contains(elementName)) {
                removedElements.add(elementName);
            }
        }
        return removedElements;
    }

    /**
     * Возвращает Annotation у Choice.
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
     * Возвращает Annotation у Element.
     */
    public static String getElementTypeAnnotation(XmlSchemaElement element) {
        return ((XmlSchemaDocumentation) element.getAnnotation().getItems().get(0)).getMarkup().item(0).getNodeValue();
    }

    /**
     * Возвращает Annotation у ComplexType.
     */
    public static String getComplexTypeAnnotation(XmlSchemaComplexType complexType) {
        String description;
        try {
            description = ((XmlSchemaDocumentation) complexType.getAnnotation().getItems().get(0)).getMarkup().item(0).getNodeValue();
            return description;
        } catch (NullPointerException ignore) {}

        return null;
    }

    /**
     * Возвращает Annotation у SimpleType.
     */
    public static String getSimpleTypeAnnotation(XmlSchemaSimpleType simpleType) {
        return ((XmlSchemaDocumentation) simpleType.getAnnotation().getItems().get(0)).getMarkup().item(0).getNodeValue();
    }

    /**
     * Возвращает все импорты схемы.
     */
    public static List<XmlSchemaImport> getSchemaImports(XmlSchema schema) {
        List<XmlSchemaImport> schemaImports = new ArrayList<XmlSchemaImport>();
        for (XmlSchemaObject schemaObject : schema.getItems()) {
            if (schemaObject instanceof XmlSchemaImport) {
                schemaImports.add((XmlSchemaImport) schemaObject);
            }
        }
        return schemaImports;
    }

    /**
     * Возвращает определение элемента схемы.
     */
    public static Map<QName, XmlSchemaElement> getElementsMap(XmlSchema schema) {
        return new HashMap<QName, XmlSchemaElement>(schema.getElements());
    }

    /**
     * Возвращает список XmlSchemaElement элементов последовательности в sequence.
     */
    public static List<XmlSchemaElement> getSequenceElements(XmlSchemaComplexContentExtension extension) {
        List<XmlSchemaElement> elements = new ArrayList<XmlSchemaElement>();
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
     * Возвращает список XmlSchemaElement элементов последовательности в sequence.
     */
    public static List<XmlSchemaElement> getSequenceElements(List<XmlSchemaSequenceMember> sequenceMembers) {
        List<XmlSchemaElement> elements = new ArrayList<XmlSchemaElement>();
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
     * Возвращает все XmlSchemaChoice элементы в последовательности.
     */
    public static List<XmlSchemaChoice> getSequenceChoices(XmlSchemaComplexContentExtension extension) {
        List<XmlSchemaChoice> elements = new ArrayList<XmlSchemaChoice>();

        for (XmlSchemaSequenceMember sequenceMember : ((XmlSchemaSequence) extension.getParticle()).getItems()) {
            if (sequenceMember instanceof XmlSchemaChoice) {
                elements.add((XmlSchemaChoice) sequenceMember);
            }
        }
        return elements;
    }

    /**
     * Возвращает все XmlSchemaChoice элементы в последовательности.
     */
    public static List<XmlSchemaChoice> getSequenceChoices(List<XmlSchemaSequenceMember> sequenceMembers) {
        List<XmlSchemaChoice> elements = new ArrayList<XmlSchemaChoice>();

        for (XmlSchemaSequenceMember sequenceMember : sequenceMembers) {
            if (sequenceMember instanceof XmlSchemaChoice) {
                elements.add((XmlSchemaChoice) sequenceMember);
            }
        }
        return elements;
    }

    /**
     * Возвращает добавленные импорты.
     */
    public static List<String> getAddedImports(TreeNode<Object> actualSchemaTree, TreeNode<Object> schemaToCompareTree) {
        List<String> addedImports = new ArrayList<String>();

        List<String> oldImports = TreeHelper.getSchemaImports(actualSchemaTree);
        List<String> newImports = TreeHelper.getSchemaImports(schemaToCompareTree);

        // если новый элемент не содержится среди старых, то значит он был добавлен
        for (String importName : newImports) {
            if (!oldImports.contains(importName)) {
                addedImports.add(importName);
            }
        }
        return addedImports;
    }

    /**
     * Возвращает удаленные импорты.
     */
    public static List<String> getRemovedImports(TreeNode<Object> actualSchemaTree, TreeNode<Object> schemaToCompareTree) {
        List<String> removedImports = new ArrayList<String>();

        List<String> oldImports = TreeHelper.getSchemaImports(actualSchemaTree);
        List<String> newImports = TreeHelper.getSchemaImports(schemaToCompareTree);

        // если старый элемент не содержится среди новых, то значит он был удален
        for (String importName : oldImports) {
            if (!newImports.contains(importName)) {
                removedImports.add(importName);
            }
        }
        return removedImports;
    }

    /**
     * Возвращает список имен SimpleType или ComplexType элементов, которые есть в обоих схемах.
     */
    public static List<String> getSameTypeNames(List<String> oldAfTypeNames, List<String> newAfTypeNames) {
        List<String> sameTypeElements = new ArrayList<String>();

        // если элемент из старой схемы содержится в новой, то добавляем в список
        for (String oldName : oldAfTypeNames) {
            if (newAfTypeNames.contains(oldName)) {
                sameTypeElements.add(oldName);
            }
        }
        return sameTypeElements;
    }

    /**
     * Возвращает список элементов с определенными именами. Порядок может не совпадать, надо тестировать.
     */
    public static List<XmlSchemaElement> getElementsFromComplexTypeBySpecificNames(List<XmlSchemaElement> elements, List<String> elementsNames) {
        List<XmlSchemaElement> specificElements = new ArrayList<XmlSchemaElement>();

        for (XmlSchemaElement element : elements) {
            if (elementsNames.contains(element.getName())) {
                specificElements.add(element);
            }
        }
        return specificElements;
    }

    /**
     * Возвращает результат сравнения SimpleType типов.
     */
    public static List<String> getDifferenceBetweenSimpleTypes(List<XmlSchemaSimpleType> oldSimpleTypes,
                                                               List<XmlSchemaSimpleType> newSimpleTypes) {
        List<String> differences = new ArrayList<String>();

        if (oldSimpleTypes.size() != newSimpleTypes.size()) {
            try {
                throw new Exception("Списки для сравнения простых типов не совпадают по длине");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (int idx = 0; idx < oldSimpleTypes.size() && idx < newSimpleTypes.size(); idx++) {

            // сравним аннотации
            String oldAnnotationDescription = SchemaHelper.getSimpleTypeAnnotation(oldSimpleTypes.get(idx));
            String newAnnotationDescription = SchemaHelper.getSimpleTypeAnnotation(newSimpleTypes.get(idx));

            if (oldAnnotationDescription != null && newAnnotationDescription != null
                    && !oldAnnotationDescription.equals(newAnnotationDescription)){
                differences.add("Для SimpleType элемента " + newSimpleTypes.get(idx).getName() + " изменено описание: " + newAnnotationDescription);
            }

            // restriction id
            String oldRestrictionId = oldSimpleTypes.get(idx).getId();
            String newRestrictionId = newSimpleTypes.get(idx).getId();

            if (oldRestrictionId != null && newRestrictionId != null
                    && !oldRestrictionId.equals(newRestrictionId)) {
                differences.add("Для SimpleType элемента " + newSimpleTypes.get(idx).getName() + " изменен id: " + newRestrictionId);
            }

            // restriction base
            String oldRestrictionBase = oldSimpleTypes.get(idx).getQName().getLocalPart();
            String newRestrictionBase = newSimpleTypes.get(idx).getQName().getLocalPart();

            if (oldRestrictionBase != null && newRestrictionBase != null
                    && !oldRestrictionBase.equals(newRestrictionBase)) {
                differences.add("Для SimpleType элемента " + newSimpleTypes.get(idx).getName() + " изменено значение base :");
            } // ignore any attributes

            // restriction facets

            List<XmlSchemaFacet> oldFacets = ((XmlSchemaSimpleTypeRestriction) oldSimpleTypes.get(idx).getContent()).getFacets();
            List<XmlSchemaFacet> newFacets = ((XmlSchemaSimpleTypeRestriction) newSimpleTypes.get(idx).getContent()).getFacets();

            // added new facets
            // removed pre existing facets
            // difference between same facets value
            List<String> facetsChanges = SchemaHelper.getDifferenceBetweenFacets(oldFacets, newFacets);

            if (facetsChanges.size() != 0) {
                for (String facetsDiffInfo : facetsChanges) {
                    differences.add("В SimpleType элементе " + newSimpleTypes.get(idx).getName() + " " + facetsDiffInfo);
                }
            }
        }
        return differences;
    }

    /**
     * Возвращает изменения в Facets. Учитывает изменения в следующих элементах:
     * XmlSchemaEnumerationFacet, XmlSchemaMaxExclusiveFacet, XmlSchemaMaxInclusiveFacet, XmlSchemaMinExclusiveFacet,
     * XmlSchemaMinInclusiveFacet, XmlSchemaPatternFacet, XmlSchemaWhiteSpaceFacet.
     *
     * А также XmlSchemaNumericFacet: XmlSchemaFractionDigitsFacet, XmlSchemaLengthFacet, XmlSchemaMaxLengthFacet,
     * XmlSchemaMinLengthFacet, XmlSchemaTotalDigitsFacet.
     */
    private static List<String> getDifferenceBetweenFacets(List<XmlSchemaFacet> oldFacets, List<XmlSchemaFacet> newFacets) {

        // список всех XmlSchemaEnumerationFacet
        List<XmlSchemaEnumerationFacet> oldEnumerationFacets = new ArrayList<XmlSchemaEnumerationFacet>();

        for (XmlSchemaFacet facet : oldFacets) {
            if (facet instanceof XmlSchemaEnumerationFacet) {
                oldEnumerationFacets.add((XmlSchemaEnumerationFacet) facet);
            }
        }

        List<XmlSchemaEnumerationFacet> newEnumerationFacets = new ArrayList<XmlSchemaEnumerationFacet>();
        for (XmlSchemaFacet facet : newFacets) {
            if (facet instanceof XmlSchemaEnumerationFacet) {
                newEnumerationFacets.add((XmlSchemaEnumerationFacet) facet);
            }
        }

        List<String> facetDiffs = new ArrayList<String>();

        // TODO: refactoring
        // ограничения enumeration
        facetDiffs.addAll(SchemaHelper.getDifferenceBetweenEnumerationFacets(oldEnumerationFacets, newEnumerationFacets));

        // ограничения Exclusive
        facetDiffs.addAll(SchemaHelper.getDifferenceBetweenMaxExclusiveFacets(oldFacets, newFacets));
        facetDiffs.addAll(SchemaHelper.getDifferenceBetweenMinExclusiveFacets(oldFacets, newFacets));

        // ограничения Inclusive
        facetDiffs.addAll(SchemaHelper.getDifferenceBetweenMaxInclusiveFacets(oldFacets, newFacets));
        facetDiffs.addAll(SchemaHelper.getDifferenceBetweenMinInclusiveFacets(oldFacets, newFacets));

        // ограничение fractionDigits
        facetDiffs.addAll(SchemaHelper.getDifferenceBetweenFractionDigitsFacets(oldFacets, newFacets));

        // ограничение length
        facetDiffs.addAll(SchemaHelper.getDifferenceBetweenLengthFacets(oldFacets, newFacets));
        // ограничение maxLength
        facetDiffs.addAll(SchemaHelper.getDifferenceBetweenMaxLengthFacets(oldFacets, newFacets));
        // ограничение minLength
        facetDiffs.addAll(SchemaHelper.getDifferenceBetweenMinLengthFacets(oldFacets, newFacets));
        // ограничение totalDigits
        facetDiffs.addAll(SchemaHelper.getDifferenceBetweenTotalDigitsFacets(oldFacets, newFacets));

        // органичение pattern
        facetDiffs.addAll(SchemaHelper.getDifferenceBetweenPatternFacets(oldFacets, newFacets));

        // органичение whiteSpace
        facetDiffs.addAll(SchemaHelper.getDifferenceBetweenWhiteSpaceFacets(oldFacets, newFacets));

        return facetDiffs;
    }

    /**
     * Добавляет информацию о изменениях в Restriction в список изменений.
     */
    private static List<String> getDiffInfoFromFacets(XmlSchemaFacet oldFacet, XmlSchemaFacet newFacet, String label) {
        List<String> diffs = new ArrayList<String>();

        // добавлено новое ограничение
        if (oldFacet == null && newFacet != null) {
            diffs.add("добавлено новое ограничение " + label + " со значением: " + newFacet.getValue());
        }

        // удалено старое органичение
        if (newFacet == null && oldFacet != null) {
            diffs.add("удалено ранее объявленное ограничение " + label + " со значением: " + oldFacet.getValue());
        }

        // изменилось значение
        if (oldFacet != null && newFacet != null && !oldFacet.getValue().equals(newFacet.getValue())) {
            diffs.add("изменено значение ограничения " + label + " с " + oldFacet.getValue() + " на " + newFacet.getValue());
        }
        return diffs;
    }

    /**
     * Ищет изменения между TotalDigits в Facet.
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
     * Ищет изменения между MaxLength в Facet.
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
     * Ищет изменения между Length в Facet.
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
     * Ищет изменения между FractionDigits в Facet.
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
     * Ищет изменения между WhiteSpace в facet.
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
     * Ищет изменения между Pattern в facet.
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
     * Ищет изменения между MinInclusive
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
     * Ищет изменения между MaxInclusive в Facets.
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
     * Ищет изменения между MinExclusive
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
     * Ищет изменения между MaxExclusive в Facets.
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
     * Изменения среди enumeration.
     */
    private static List<String> getDifferenceBetweenEnumerationFacets(List<XmlSchemaEnumerationFacet> oldEnumerationFacets, List<XmlSchemaEnumerationFacet> newEnumerationFacets) {
        List<String> diffs = new ArrayList<String>();

        List<String> oldEnumerationValues = new ArrayList<String>();
        for (XmlSchemaEnumerationFacet facet : oldEnumerationFacets) {
            oldEnumerationValues.add((String) facet.getValue());
        }

        List<String> newEnumerationValues = new ArrayList<String>();
        for (XmlSchemaEnumerationFacet facet : newEnumerationFacets) {
            newEnumerationValues.add((String) facet.getValue());
        }

        // добавлены новые enumeration
        List<String> getNewEnumerationValues = SchemaHelper.getNewEnumerationValues(oldEnumerationValues, newEnumerationValues);

        // удалены старые enumeration
        List<String> getPreExistingEnumerationValues = SchemaHelper.getPreExistingEnumerationValues(oldEnumerationValues, newEnumerationValues);

        diffs.addAll(getNewEnumerationValues);
        diffs.addAll(getPreExistingEnumerationValues);

        return diffs;
    }

    /**
     * Находит новые Enumeration values.
     */
    private static List<String> getNewEnumerationValues(List<String> oldEnumerationValues, List<String> newEnumerationValues) {
        List<String> addedEnumerations = new ArrayList<String>();
        for (String value : newEnumerationValues) {
            if (!oldEnumerationValues.contains(value)) {
                addedEnumerations.add("Добавлен новый элемент Enumeration со значением: " + value);
            }
        }
        return addedEnumerations;
    }

    /**
     * Находит ранее существовавшие Enumeration values.
     */
    private static List<String> getPreExistingEnumerationValues(List<String> oldEnumerationValues, List<String> newEnumerationValues) {
        List<String> removedEnumerations = new ArrayList<String>();
        for (String value : oldEnumerationValues) {
            if (!newEnumerationValues.contains(value)) {
                removedEnumerations.add("Удален ранее существующий Enumeration со значением: " + value);
            }
        }
        return removedEnumerations;
    }
}
