package ru.vsushko.analyzer.schema;

import org.apache.log4j.Logger;
import ru.vsushko.analyzer.schema.tree.TreeHelper;
import ru.vsushko.analyzer.schema.tree.TreeNode;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by vsa
 * Date: 21.12.14.
 */
public class DifferenceResolverImpl implements DifferenceResolver {
    private static Logger log = Logger.getLogger(SchemaInfo.class);

    private List<String> differences;

    public DifferenceResolverImpl() {
        differences = new ArrayList<String>();
    }

    /**
     * Проверка на изменения в описании схемы.
     */
    public void checkDifferenceBetweenSchemaDescription(TreeNode<Object> actualSchemaTree, TreeNode<Object> schemaToCompareTree) {
        String oldSchemaDescription = TreeHelper.getSchemaDescription(actualSchemaTree);
        String newSchemaDescription = TreeHelper.getSchemaDescription(schemaToCompareTree);

        if (!oldSchemaDescription.equals(newSchemaDescription)) {
            differences.add(encodeString("Изменено описание документа: " + newSchemaDescription));
        }
    }

    /**
     * Проверка на изменения в импортах.
     */
    public void checkDifferenceBetweenSchemaImports(TreeNode<Object> actualSchemaTree, TreeNode<Object> schemaToCompareTree) {
        List<String> addedImports = SchemaHelper.getAddedImports(actualSchemaTree, schemaToCompareTree);
        List<String> removedImports = SchemaHelper.getRemovedImports(actualSchemaTree, schemaToCompareTree);

        if (addedImports.size() != 0) {
            for (String importString : addedImports) {
                differences.add(encodeString("Добавлена зависимость структуры электронного документа от " + getSchemaImportName(importString) + "\n"));
            }
        }

        if (removedImports.size() != 0) {
            for (String importString : removedImports) {
                differences.add(encodeString("Удалена прямая зависимость структуры электронного документа от " + getSchemaImportName(importString) + "\n"));
            }
        }
    }

    /**
     * Извлекает из импорта зависимость от схемы, и строит строку "Имя схемы" + расширение.
     */
    private String getSchemaImportName(String importString) {
        String[] bits = importString.split(":");
        // позиция имени схемы всегда фиксированна
        return bits[bits.length - 2] + ".xsd";
    }

    /**
     * Проверка на изменения в SimpleType элементах.
     */
    public void checkDifferenceBetweenSimpleTypes(TreeNode<Object> actualSchemaTree, TreeNode<Object> schemaToCompareTree) {

        // найдем список имен всех SimpleType
        List<String> oldAfSimpleTypeNames = TreeHelper.getSimpleTypesNamesFromTree(actualSchemaTree);
        List<String> newAfSimpleTypeNames = TreeHelper.getSimpleTypesNamesFromTree(schemaToCompareTree);

        // проверка на сущестсование новых SimpleType типов
        List<String> newSimpleTypes = SchemaHelper.getNewSimpleTypes(oldAfSimpleTypeNames, newAfSimpleTypeNames);

        if (newSimpleTypes.size() != 0) {
            for (String element : newSimpleTypes) {
                differences.add(encodeString("Добавлен локальный тип " + element + "\n"));
            }
        }

        // проверка на то, были ли удалены SimpleType типы объявленные ранее
        List<String> preExistingSimpleTypes =
                SchemaHelper.getPreExistingSimpleTypes(oldAfSimpleTypeNames, newAfSimpleTypeNames);

        if (preExistingSimpleTypes.size() != 0) {
            for (String element : preExistingSimpleTypes) {
                differences.add(encodeString("Удален элемент " + element + "\n"));
            }
        }

        // получим список SimpleType, у которых имена совпадают
        List<String> sameSimpleTypeNames = SchemaHelper.getSameTypeNames(oldAfSimpleTypeNames, newAfSimpleTypeNames);

        // список может быть пустым, потому что SimpleType типов нету в схеме
        if (sameSimpleTypeNames.size() != 0) {
            // проверка на изменения в SimpleType
            List<String> simpleTypeChanges = SchemaHelper.getDifferenceBetweenSimpleTypes(
                    TreeHelper.getSimpleTypesFromSchemaBySpecificNames(actualSchemaTree, sameSimpleTypeNames),
                    TreeHelper.getSimpleTypesFromSchemaBySpecificNames(schemaToCompareTree, sameSimpleTypeNames));

            if (simpleTypeChanges.size() != 0) {
                for (String diffString : simpleTypeChanges) {
                    differences.add(encodeString(diffString));
                }
            }
        }
    }

    /**
     * Проверка на существование новых ComplexType типов.
     */
    public void checkDifferenceBetweenComplexTypes(TreeNode<Object> actualSchemaTree, TreeNode<Object> schemaToCompareTree) {
        // найдем список имен всех ComplexType
        List<String> oldAfComplexTypeNames = TreeHelper.getComplexTypesNamesFromTree(actualSchemaTree);
        List<String> newAfComplexTypeNames = TreeHelper.getComplexTypesNamesFromTree(schemaToCompareTree);

        // нет ComplexType элементов в схеме
        if (oldAfComplexTypeNames.size() != 0 && newAfComplexTypeNames.size() != 0) {
            // проверка на существование новых ComplexType типов
            List<String> newComplexTypes = SchemaHelper.getNewComplexTypes(oldAfComplexTypeNames, newAfComplexTypeNames);

            if (newComplexTypes.size() != 0) {
                for (String element : newComplexTypes) {
                    differences.add(encodeString("Добавлен локальный тип " + element + "\n"));
                }
            }

            // проверка на то, были ли удалены элементы ComplexType объявленные ранее
            List<String> preExistingComplexTypes =
                    SchemaHelper.getPreExistingComplexTypes(oldAfComplexTypeNames, newAfComplexTypeNames);

            if (preExistingComplexTypes.size() != 0) {
                for (String element : preExistingComplexTypes) {
                    differences.add(encodeString("Удален элемент " + element + "\n"));
                }
            }

            // получим список ComplexType, у которых имена совпадают
            List<String> sameComplexTypeNames = SchemaHelper.getSameTypeNames(oldAfComplexTypeNames, newAfComplexTypeNames);

            // список может быть пустым, потому что ComplexType элементов нету в схеме
            if (sameComplexTypeNames.size() != 0) {
                // проверка на изменения в ComplexType
                List<String> complexTypeChanges = SchemaHelper.getDifferenceBetweenComplexTypes(
                        TreeHelper.getComplexTypesFromSchemaBySpecificNames(actualSchemaTree, sameComplexTypeNames),
                        TreeHelper.getComplexTypesFromSchemaBySpecificNames(schemaToCompareTree, sameComplexTypeNames));

                if (complexTypeChanges.size() != 0) {
                    for (String diffString : complexTypeChanges) {
                        differences.add(encodeString(diffString));
                    }
                }
            }
        }
    }

    /**
     * Все изменения в схемах.
     */
    public void findAllDifference(TreeNode<Object> actualSchemaTree, TreeNode<Object> schemaToCompareTree) {
        checkDifferenceBetweenSchemaDescription(actualSchemaTree, schemaToCompareTree);
        checkDifferenceBetweenSchemaImports(actualSchemaTree, schemaToCompareTree);
        checkDifferenceBetweenSimpleTypes(actualSchemaTree, schemaToCompareTree);
        checkDifferenceBetweenComplexTypes(actualSchemaTree, schemaToCompareTree);
    }

    /**
     * Возвращает список изменений, выявленных при сравнении двух схем.
     */
    @Override
    public List<String> getDifferences() {
        return differences;
    }

    /**
     * Кодирут в ISO-8859-1 для JTextArea.
     */
    public String encodeString(String s) {
        String result = "";
        try {
            result = new String(s.getBytes("UTF-8"), "ISO-8859-1");
            return result;
        } catch (UnsupportedEncodingException e) {
            log.debug("Ошибка при кодировании строки: кодировка ISO-8859-1 не поддерживается");
        }
        return result;
    }
}
