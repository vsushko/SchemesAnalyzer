package ru.vsushko.analyzer;

import com.sun.org.apache.xerces.internal.dom.DeferredTextImpl;
import junit.framework.Assert;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaDocumentation;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ru.vsushko.analyzer.schema.DifferenceResolver;
import ru.vsushko.analyzer.schema.DifferenceResolverImpl;
import ru.vsushko.analyzer.schema.InfoProvider;
import ru.vsushko.analyzer.schema.SchemaHelper;
import ru.vsushko.analyzer.schema.tree.TreeHelper;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Created by vsa
 * Date: 08.12.14.
 */
public class XmlSchemaTest {
    private static final String PATH_TO_SCHEMAS = "c:\\Users\\vsa\\Documents\\schemas\\first\\";
    private static final String ANOTHER_PATH_TO_SCHEMAS = "c:\\Users\\vsa\\Documents\\schemas\\second\\";
    private InfoProvider provider;
    private XmlSchema schemeToCompare;
    private XmlSchema actualScheme;
    private String actualSchemeFullPath;
    private String schemeToCompareFullPath;

    @Before
    public void setUp() {

//        actualSchemeFullPath = PATH_TO_SCHEMAS + provider.getSchemaName(0);
//        schemeToCompareFullPath = ANOTHER_PATH_TO_SCHEMAS + provider.getSchemaName(0);

        // Используем Apache XmlSchema
//        actualScheme = SchemaHelper.getSchemaFromPath(actualSchemeFullPath);
//        schemeToCompare = SchemaHelper.getSchemaFromPath(schemeToCompareFullPath);

    }

    @Test
    public void readSchema() {
        Assert.assertNotNull(actualScheme);
        Assert.assertNotNull(schemeToCompare);
    }

    @Test
    public void readSchemaTargetNamespace() {
        Assert.assertNotNull(schemeToCompare.getTargetNamespace());
        Assert.assertTrue(schemeToCompare.getTargetNamespace() instanceof String);

        System.out.println(schemeToCompare.getTargetNamespace());
    }

    @Test
    public void readSchemaAnnotationDocumentationWithXmlSchema() {
        XmlSchemaDocumentation xmlSchemaDocumentation = (XmlSchemaDocumentation) schemeToCompare.getAnnotation().getItems().get(0);
        Assert.assertNotNull(xmlSchemaDocumentation);

        NodeList nodeList = xmlSchemaDocumentation.getMarkup();
        Assert.assertNotNull(nodeList);

        // Достаем описание из first child
        String schemaDescription = nodeList.item(0).getNodeValue();

        Assert.assertNotNull(schemaDescription);
        System.out.println("XmlSchema description: " + schemaDescription);
    }

    /**
     * Достаем из схемы версию её версию.
     */
    @Test
    public void identifySchemaVersionByTargetNamespace() {
        String schemaVersion = SchemaHelper.readSchemaVersionInfo(schemeToCompare.getTargetNamespace());
        Assert.assertNotNull(schemaVersion);
    }

    /**
     * Достаем из документа первое вхождение.
     * <xs:annotation>
     * <xs:documentation>Описание документа </xs:documentation>
     * </xs:annotation>
     */
    @Test
    public void readSchemaDescription() {
        try {
            FileInputStream file = new FileInputStream(new File(ANOTHER_PATH_TO_SCHEMAS + provider.getSchemaName(0)));
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();

            Document xmlDocument = builder.parse(file);
            XPath xPath = XPathFactory.newInstance().newXPath();

            String expression = "//annotation/documentation";
            Node node = (Node) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODE);

            String schemaDescription = ((DeferredTextImpl) node.getFirstChild()).getData();

            Assert.assertNotNull(schemaDescription);
            System.out.println(schemaDescription);

        } catch (ParserConfigurationException e) {
            System.out.println("Не удалось создать фабрику");
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            System.out.println("Не удалось прочитать файл");
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Для каждого ComplexType типа, если каждый тип существовал, то круто.
     */
    @Test
    public void checkTypeExistEarlier() {
        // достанем все имена из схемы предыдущего формата
        List<String> actualTypeNames = SchemaHelper.getComplexTypeListFromSchema(actualScheme);

        // достанем все имена из схемы сравниваемого формата
        List<String> typeNamesToCompare = SchemaHelper.getComplexTypeListFromSchema(schemeToCompare);

        // если размер коллекций совпадает, то ок
        Assert.assertEquals(actualTypeNames.size(), typeNamesToCompare.size());

        // все ли ComplexType совпадают
        Assert.assertTrue(typeNamesToCompare.containsAll(actualTypeNames));
    }

    /**
     * Добавились новые ComplexType элементы. Просто поиск новых элементов.
     */
    @Test
    public void checkSchemaComplexTypesHaveNewElements() {
        List<String> newElements = SchemaHelper.getNewComplexTypes(
                SchemaHelper.getComplexTypeListFromSchema(actualScheme),
                SchemaHelper.getComplexTypeListFromSchema(schemeToCompare));

        Assert.assertNotNull(newElements);

        // в тестовых xml-документах все элементы совпадают, пока...
        Assert.assertTrue(newElements.size() == 0);
    }

    /**
     * Удалились ли ComplexType элементы ранее объявленные. Поиск и проверка.
     */
    @Test
    public void checkSchemaComplexTypesHaveRemovedElements() {
        List<String> preExistingElements = SchemaHelper.getPreExistingComplexTypes(
                SchemaHelper.getComplexTypeListFromSchema(actualScheme),
                SchemaHelper.getComplexTypeListFromSchema(schemeToCompare));

        Assert.assertNotNull(preExistingElements);

        // в тестовых xml-документах все ComplexType совпадают, пока...
        Assert.assertTrue(preExistingElements.size() == 0);
    }

    /**
     * Поменялись ли типы данных.
     */
    @Test
    public void checkSchemaComplexTypesHaveChangedTypesOfElementsInSequence() {
        // найдем список имен всех ComplexType
        List<String> oldAfComplexTypeNames = TreeHelper.getComplexTypesNamesFromTree(TreeHelper.buildSchemaTree(actualScheme, actualSchemeFullPath));
        List<String> newAfComplexTypeNames = TreeHelper.getComplexTypesNamesFromTree(TreeHelper.buildSchemaTree(schemeToCompare, schemeToCompareFullPath));

        // получим список ComplexType, у которых имена совпадают
        List<String> sameComplexTypeNames = SchemaHelper.getSameTypeNames(oldAfComplexTypeNames, newAfComplexTypeNames);

        List<String> diffStrings = SchemaHelper.getDifferenceBetweenComplexTypes(
                TreeHelper.getComplexTypesFromSchemaBySpecificNames(TreeHelper.buildSchemaTree(actualScheme, actualSchemeFullPath), sameComplexTypeNames),
                TreeHelper.getComplexTypesFromSchemaBySpecificNames(TreeHelper.buildSchemaTree(schemeToCompare, schemeToCompareFullPath), sameComplexTypeNames)
        );

        // Временно проверка на null, далее проверка на size
        Assert.assertNotNull(diffStrings);

    }

    /**
     * Были ли добавлены новые SimpleType.
     */
    @Test
    public void checkSchemaSimpleTypesHaveNewElements() {
        List<String> newElements = SchemaHelper.getNewSimpleTypes(
                SchemaHelper.getSimpleTypeListFromSchema(actualScheme),
                SchemaHelper.getSimpleTypeListFromSchema(schemeToCompare));

        Assert.assertNotNull(newElements);

        // в тестовых xml-документах все элементы совпадают, пока...
        Assert.assertTrue(newElements.size() == 0);
    }

    /**
     * Были ли удалены старые SimpleType.
     */
    @Test
    public void checkSchemaSimpleTypesHadRemovedElements() {
        List<String> preExistingElements = SchemaHelper.getPreExistingSimpleTypes(
                SchemaHelper.getSimpleTypeListFromSchema(actualScheme),
                SchemaHelper.getSimpleTypeListFromSchema(schemeToCompare));

        Assert.assertNotNull(preExistingElements);

        // в тестовых xml-документах все ComplexType совпадают, пока...
        Assert.assertTrue(preExistingElements.size() == 0);
    }

    /**
     * Смотрит, было ли изменено описание схемы
     */
    @Test
    public void compareSchemaDescription() {
        // получим список изменений
        DifferenceResolver resolver = new DifferenceResolverImpl();

        resolver.checkDifferenceBetweenSchemaDescription(
                TreeHelper.buildSchemaTree(actualScheme, actualSchemeFullPath),
                TreeHelper.buildSchemaTree(schemeToCompare, schemeToCompareFullPath));

        List<String> diffs = resolver.getDifferences();

        Assert.assertNotNull(diffs);

        // измнения есть
        Assert.assertEquals(diffs.size(), 1);
    }

    @Test
    public void checkForAddedOrRemovedImportsBetweenSchemas() {
        DifferenceResolver resolver = new DifferenceResolverImpl();

        resolver.checkDifferenceBetweenSchemaImports(
                TreeHelper.buildSchemaTree(actualScheme, actualSchemeFullPath),
                TreeHelper.buildSchemaTree(schemeToCompare, schemeToCompareFullPath)
        );

        List<String> diffs = resolver.getDifferences();

        Assert.assertNotNull(diffs);

        // изменения есть
        Assert.assertEquals(diffs.size(), 1);
    }

    /**
     * Ищет новые SimpleType элементы в деревьях.
     */
    @Test
    public void checkDifferenceBetweenSimpleTypes() {
        DifferenceResolver resolver = new DifferenceResolverImpl();

        resolver.checkDifferenceBetweenSimpleTypes(
                TreeHelper.buildSchemaTree(actualScheme, actualSchemeFullPath),
                TreeHelper.buildSchemaTree(schemeToCompare, schemeToCompareFullPath)
        );

        List<String> diffs = resolver.getDifferences();

        Assert.assertNotNull(diffs);

        // изменения есть
        Assert.assertEquals(diffs.size(), 1);
    }

    /**
     * Ищет новые ComplexType в деревьях.
     */
    @Test
    public void checkDifferenceBetweenComplexTypes() {
        DifferenceResolver resolver = new DifferenceResolverImpl();

        resolver.checkDifferenceBetweenComplexTypes(
                TreeHelper.buildSchemaTree(actualScheme, actualSchemeFullPath),
                TreeHelper.buildSchemaTree(schemeToCompare, schemeToCompareFullPath)
        );

        List<String> diffs = resolver.getDifferences();

        Assert.assertNotNull(diffs);

        // изменения есть
        Assert.assertEquals(diffs.size(), 1);
    }

    @Test
    public void extractSchemaNameFromImport() {
        String schemaImportText = "urn:company.ru:Information:CompanyDocuments:CompanyTypes:1.1.1";

        String[] bits = schemaImportText.split(":");
        String result = bits[bits.length - 2] + ".xsd";

        Assert.assertEquals(result, "Document.xsd");
    }

}